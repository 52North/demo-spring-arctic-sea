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

import javax.inject.Inject;

import org.apache.http.client.utils.URIBuilder;
import org.n52.svalbard.decode.DecoderRepository;
import org.n52.svalbard.encode.EncoderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoConfig {

	@Inject
	private DecoderRepository decoderRepository;

	@Inject
	private EncoderRepository encoderRespository;

	@Value("${sos.path:/52n-sos-webapp/service}")
	private String sosPath;

	@Value("${sos.host:localhost}")
	private String sosHost;

	@Value("${sos.port:8080}")
	private int sosPort;

	public String getSOSPath() {
		return sosPath;
	}

	public String getSOSHost() {
		return sosHost;
	}

	public int getSOSPort() {
		return sosPort;
	}

	public URIBuilder getURIBuilder() {
		URIBuilder uriBuilder = new URIBuilder();
		uriBuilder.setScheme("http");
		uriBuilder.setHost(sosHost);
		uriBuilder.setPort(sosPort);
		uriBuilder.setPath(sosPath);
		return uriBuilder;
	}

	public DecoderRepository getDecoderRepository() {
		return decoderRepository;
	}

	public EncoderRepository getEncoderRepository() {
		return encoderRespository;
	}
}
