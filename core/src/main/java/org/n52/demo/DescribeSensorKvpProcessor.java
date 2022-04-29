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

import java.net.URISyntaxException;

import org.n52.demo.web.GetDescribeSensor;
import org.n52.demo.web.HttpClientHandler;
import org.n52.demo.web.Response;
import org.n52.shetland.ogc.ows.exception.OwsExceptionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DescribeSensorKvpProcessor extends AbstractRequestProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(DescribeSensorKvpProcessor.class);

	private String procedure;

	private String version;

	private String format;

	public DescribeSensorKvpProcessor(DemoConfig demoConfig, HttpClientHandler httpClientHandler, String procedure,
			String version, String format) {
		super(demoConfig, httpClientHandler);
		this.procedure = procedure;
		this.version = version;
		this.format = format;
	}

	@Override
	public Response process() {
		try {
			return query(new GetDescribeSensor().withProcedure(procedure).withVersion(version).withFormat(format));
		} catch (URISyntaxException | OwsExceptionReport e) {
			LOG.error("Error while processing DescribeSensor request!", e);
		}
		return null;
	}

}
