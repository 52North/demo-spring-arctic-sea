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

import java.io.IOException;
import java.net.URISyntaxException;

import javax.inject.Inject;

import org.n52.demo.web.HttpClientHandler;
import org.n52.demo.web.Response;
import org.n52.janmayen.http.MediaType;
import org.n52.janmayen.http.MediaTypes;
import org.n52.shetland.ogc.ows.exception.OwsExceptionReport;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import org.n52.shetland.ogc.ows.service.OwsServiceResponse;
import org.n52.shetland.ogc.sensorML.SensorML20Constants;
import org.n52.shetland.ogc.sensorML.SensorMLConstants;
import org.n52.shetland.ogc.sos.Sos1Constants;
import org.n52.shetland.ogc.sos.Sos2Constants;
import org.n52.shetland.ogc.sos.SosCapabilities;
import org.n52.shetland.ogc.sos.SosObservationOffering;
import org.n52.shetland.ogc.sos.request.DescribeSensorRequest;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.encode.exception.EncodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Processor {

	private static final Logger LOG = LoggerFactory.getLogger(Processor.class);

	@Inject
	private HttpClientHandler httpClientHandler;
	@Inject
	private DemoConfig demoConfig;

	public Processor() {

	}

	public void process() throws IOException, URISyntaxException, EncodingException, OwsExceptionReport {
		OwsServiceResponse response = requestGetCapabilitiesKVP100();
		if (response != null && response instanceof GetCapabilitiesResponse) {
			GetCapabilitiesResponse getCaps = (GetCapabilitiesResponse) response;
			SosCapabilities capabilities = (SosCapabilities) getCaps.getCapabilities();
			if (capabilities.getContents().isPresent()) {
				SosObservationOffering offering = capabilities.getContents().get().first();
				for (String procdure : offering.getProcedures()) {
					requestDescribeSensorKvp100(procdure, SensorMLConstants.SENSORML_OUTPUT_FORMAT_MIME_TYPE);
					requestDescribeSensorPox100(procdure, SensorMLConstants.SENSORML_OUTPUT_FORMAT_MIME_TYPE);
				}
			}
		}

		response = requestGetCapabilitiesKVP20();
		if (response != null && response instanceof GetCapabilitiesResponse) {
			GetCapabilitiesResponse getCaps = (GetCapabilitiesResponse) response;
			SosCapabilities capabilities = (SosCapabilities) getCaps.getCapabilities();
			if (capabilities.getContents().isPresent()) {
				SosObservationOffering offering = capabilities.getContents().get().first();
				for (String procdure : offering.getProcedures()) {
					requestDescribeSensorKvp200(procdure, SensorML20Constants.NS_SML_20);
					requestDescribeSensorPox200(procdure, SensorML20Constants.NS_SML_20);
				}
			}
		}
	}

	private OwsServiceResponse requestGetCapabilitiesKVP100() {
		return (OwsServiceResponse) process(
				new GetCapabilitiesKvpProcessor(demoConfig, httpClientHandler, Sos1Constants.SERVICEVERSION));
	}

	private void requestDescribeSensorKvp100(String procdure, String format) {
		process(new DescribeSensorKvpProcessor(demoConfig, httpClientHandler, procdure, Sos1Constants.SERVICEVERSION,
				format));
	}

	private void requestDescribeSensorPox100(String procdure, String outputFormat) {
		DescribeSensorRequest request = new DescribeSensorRequest(Sos1Constants.SOS, Sos1Constants.SERVICEVERSION);
		request.setProcedure(procdure);
		request.setProcedureDescriptionFormat(outputFormat);
		process(new DescribeSensorPoxProcessor(demoConfig, httpClientHandler, request));
	}

	private OwsServiceResponse requestGetCapabilitiesKVP20() {
		return (OwsServiceResponse) process(
				new GetCapabilitiesKvpProcessor(demoConfig, httpClientHandler, Sos2Constants.SERVICEVERSION));
	}

	private void requestDescribeSensorKvp200(String procdure, String format) {
		process(new DescribeSensorKvpProcessor(demoConfig, httpClientHandler, procdure, Sos2Constants.SERVICEVERSION,
				format));
	}
	
	private void requestDescribeSensorPox200(String procdure, String procedureDescriptionFormat) {
		DescribeSensorRequest request = new DescribeSensorRequest(Sos2Constants.SOS, Sos2Constants.SERVICEVERSION);
		request.setProcedure(procdure);
		request.setProcedureDescriptionFormat(procedureDescriptionFormat);
		process(new DescribeSensorPoxProcessor(demoConfig, httpClientHandler, request));
	}

	private Object process(AbstractRequestProcessor processor) {
		try {
			Response response = processor.process();
			if (response != null) {
				Object decode = new ResponseDecoder(demoConfig).decode(response);
				if (checkForEception(decode)) {
					LOG.info("Exception returned {}", decode);
				} else {
					LOG.info("Returned  response {}", decode);
				}
				return decode;
			}
		} catch (OwsExceptionReport | DecodingException e) {
			LOG.error("Error while checking for procedures", e);
		}
		return null;
	}

	private boolean checkForEception(Object response) {
		return response != null && response instanceof OwsExceptionReport;
	}

	protected MediaType getDefaultContentType() {
		return MediaTypes.APPLICATION_XML;
	}

}
