/*
 * Copyright (C) 2022 52�North Spatial Information Research GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.demo.web;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.n52.demo.DemoConfig;
import org.n52.janmayen.http.MediaType;
import org.n52.janmayen.lifecycle.Constructable;
import org.n52.janmayen.lifecycle.Destroyable;
import org.n52.shetland.ogc.ows.exception.CodedException;
import org.n52.shetland.ogc.ows.exception.NoApplicableCodeException;
import org.n52.shetland.ogc.ows.exception.OwsExceptionReport;
import org.n52.shetland.util.CollectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

@Component
public class HttpClientHandler implements Constructable, Destroyable {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientHandler.class);

    private static final RetryPolicy<HttpResponse> RETRY_POLICY =
            new RetryPolicy<HttpResponse>().withDelay(10, 900, ChronoUnit.SECONDS)
                    .handle(ConnectException.class);
    
    private static final int TIMEOUT = 60 * 60 * 1000;

    @Inject
	private DemoConfig demoConfig;
    
	private CacheConfig cacheConfig;

	private RequestConfig requestConfig;

	private CloseableHttpClient client;

	public Response execute(URI url, AbstractRequest request) throws OwsExceptionReport {
		if (request instanceof AbstractGetRequest) {
			return doGet(url, (AbstractGetRequest) request);
		} else if (request instanceof AbstractPostRequest) {
			return doPost(url, (AbstractPostRequest<?>) request);
		}
		throw new NoApplicableCodeException().withMessage("The request type '%s' is unknown!",
				request.getClass().getTypeName());
	}

	protected Response doGet(URI url, AbstractGetRequest request) throws OwsExceptionReport {
		try {
			HttpGet httpGet = new HttpGet(getGetUrl(url, request.getPath(), request.getQueryParameters()));
			if (request.hasHeader()) {
				for (Entry<String, String> entry : request.getHeader().entrySet()) {
					httpGet.addHeader(entry.getKey(), entry.getValue());
				}
			}
			logRequest(getGetUrl(url, request.getPath(), request.getQueryParameters()));
			return getContent(executeHttpRequest(httpGet));
		} catch (URISyntaxException | IOException e) {
			throw new NoApplicableCodeException().causedBy(e);
		}
	}

	protected Response doGet(URI url, String path, Map<String, String> header, Map<String, String> parameter)
			throws OwsExceptionReport {
		try {
			HttpGet httpGet = new HttpGet(getGetUrl(url, path, parameter));
			if (CollectionHelper.isNotEmpty(header)) {
				for (Entry<String, String> entry : header.entrySet()) {
					httpGet.addHeader(entry.getKey(), entry.getValue());
				}
			}
			logRequest(getGetUrl(url, path, parameter));
			return getContent(executeHttpRequest(httpGet));
		} catch (URISyntaxException | IOException e) {
			throw new NoApplicableCodeException().causedBy(e);
		}
	}

	protected Response doPost(URI url, AbstractPostRequest<?> request) throws CodedException {
		try {
			HttpPost httpPost = new HttpPost(getPathUrl(url, request.getPath()));
			if (request.hasHeader()) {
				for (Entry<String, String> entry : request.getHeader().entrySet()) {
					httpPost.addHeader(entry.getKey(), entry.getValue());
				}
			}
			String content = request.getContent();
			logRequest(content);
			httpPost.setEntity(new StringEntity(content));
			return getContent(executeHttpRequest(httpPost));
		} catch (IOException | URISyntaxException e) {
			throw new NoApplicableCodeException().causedBy(e);
		}
	}

	protected Response doPost(URI url, String content, MediaType contentType) throws CodedException {
		try {
			HttpPost httpPost = new HttpPost(url);
			httpPost.addHeader(HttpHeaders.CONTENT_TYPE, contentType.toString());
			logRequest(content);
			httpPost.setEntity(new StringEntity(content));
			return getContent(executeHttpRequest(httpPost));
		} catch (IOException e) {
			throw new NoApplicableCodeException().causedBy(e);
		}
	}

	private CloseableHttpResponse executeHttpRequest(HttpRequestBase request) throws IOException {
		int counter = 4;
		CloseableHttpResponse response = null;
		long start = System.currentTimeMillis();
		do {
			response = Failsafe.with(RETRY_POLICY)
					.onFailure(ex -> LOGGER.warn("Could not connect to host; retrying", ex))
					.get(() -> getClient().execute(request));
		} while (response == null && counter >= 0);

		LOGGER.trace("Querying took {} ms!", System.currentTimeMillis() - start);
		return response;
	}

	private Response getContent(CloseableHttpResponse response) throws IOException {
		try {
			return response != null
					? new Response(response.getStatusLine().getStatusCode(),
							response.getEntity() != null ? EntityUtils.toString(response.getEntity(), "UTF-8") : null)
					: new Response(200, null);
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	private URI getGetUrl(URI url, Map<String, String> parameters) throws URISyntaxException {
		URIBuilder uriBuilder = demoConfig.getURIBuilder();
		if (CollectionHelper.isNotEmpty(parameters)) {
			for (Entry<String, String> entry : parameters.entrySet()) {
				uriBuilder.addParameter(entry.getKey(), entry.getValue());
			}
		}
		URI uri = uriBuilder.build();
		return uri;
	}

	private URI getGetUrl(URI url, String path, Map<String, String> parameters) throws URISyntaxException {
		return getGetUrl(getPathUrl(url, path), parameters);
	}

	private URI getPathUrl(URI url, String path) throws URISyntaxException {
		if (!Strings.isNullOrEmpty(path)) {
			URIBuilder uriBuilder = new URIBuilder(url.toString() + path, StandardCharsets.UTF_8);
			return uriBuilder.build();
		}
		return url;
	}

	private CloseableHttpClient getClient() {
		return client;
	}

	private void logRequest(URI request) {
		logRequest(request.toString());
	}

	private void logRequest(String request) {
		LOGGER.debug("Request: {}", request);
	}

	@PostConstruct
	public void init() {
		this.cacheConfig = CacheConfig.custom().setMaxCacheEntries(1000).setMaxObjectSize(8192).build();
		this.client = CachingHttpClients.custom().setCacheConfig(cacheConfig)
				.setDefaultRequestConfig(getRequestConfig()).setSSLSocketFactory(getSSLSocketFactory())
				.setMaxConnTotal(200).setMaxConnPerRoute(50).useSystemProperties().build();
	}

	private SSLConnectionSocketFactory getSSLSocketFactory() {
		if (isIgnoreSSLHostnameValidation()) {
			LOGGER.debug("Noop hostname verifier enabled!");
			try {
				return new SSLConnectionSocketFactory(
						SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build(),
						NoopHostnameVerifier.INSTANCE);
			} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
				LOGGER.error("Error creating SSL connnection socket factory!", e);
			}
		}
		return null;
	}

	private boolean isIgnoreSSLHostnameValidation() {
		return true;
	}

	private RequestConfig getRequestConfig() {
		Builder builder = RequestConfig.custom().setConnectTimeout(TIMEOUT).setConnectionRequestTimeout(TIMEOUT)
				.setSocketTimeout(TIMEOUT);
		return builder.build();
	}

	@PreDestroy
	public void destroy() {
		if (requestConfig != null) {
			requestConfig = null;
		}
		if (cacheConfig != null) {
			cacheConfig = null;
		}
	}
}