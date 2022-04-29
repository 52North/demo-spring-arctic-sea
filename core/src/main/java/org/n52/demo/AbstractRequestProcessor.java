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
package org.n52.demo;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.xmlbeans.XmlObject;
import org.n52.demo.web.AbstractRequest;
import org.n52.demo.web.HttpClientHandler;
import org.n52.demo.web.Response;
import org.n52.demo.web.XmlPostRequest;
import org.n52.janmayen.http.MediaTypes;
import org.n52.shetland.ogc.ows.exception.OwsExceptionReport;
import org.n52.shetland.ogc.ows.service.OwsOperationKey;
import org.n52.shetland.ogc.ows.service.OwsServiceRequest;
import org.n52.svalbard.encode.Encoder;
import org.n52.svalbard.encode.EncoderKey;
import org.n52.svalbard.encode.OperationRequestEncoderKey;
import org.n52.svalbard.encode.exception.EncodingException;

public abstract class AbstractRequestProcessor implements Runnable {

	private DemoConfig demoConfig;

	private HttpClientHandler httpClientHandler;

	public AbstractRequestProcessor(DemoConfig demoConfig, HttpClientHandler httpClientHandler) {
		this.demoConfig = demoConfig;
		this.httpClientHandler = httpClientHandler;
	}

	public abstract Response process();

	@Override
	public void run() {
		process();
	}

	protected Response query(AbstractRequest request) throws URISyntaxException, OwsExceptionReport {
		return checkStatus(httpClientHandler.execute(getURL(), request));
	}

	protected AbstractRequest createRequest(OwsServiceRequest request) throws EncodingException {
		return new XmlPostRequest(encode(request));
	}

	protected XmlObject encode(OwsServiceRequest request) throws EncodingException {
		EncoderKey key = getEncoderKey(request);
		Encoder<XmlObject, OwsServiceRequest> encoder = getEncoder(key);
		if (encoder != null) {
			return encoder.encode(request);
		}
		return null;
	}

	protected Encoder<XmlObject, OwsServiceRequest> getEncoder(EncoderKey key) {
		return demoConfig.getEncoderRepository().getEncoder(key);
	}

	protected EncoderKey getEncoderKey(OwsServiceRequest request) {
		OwsOperationKey key = new OwsOperationKey(request.getService(), request.getVersion(),
				request.getOperationName());
		return new OperationRequestEncoderKey(key, MediaTypes.APPLICATION_XML);
	}

	protected URI getURL() throws URISyntaxException {
		return demoConfig.getURIBuilder().build();
	}

	protected Response checkStatus(Response response) {
		if (response.getStatus() != 200) {
		}
		return response;
	}
}
