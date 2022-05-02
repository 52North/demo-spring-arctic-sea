package org.n52.demo;

import java.net.URISyntaxException;

import org.n52.demo.web.GetDescribeSensor;
import org.n52.demo.web.HttpClientHandler;
import org.n52.demo.web.Response;
import org.n52.shetland.ogc.ows.exception.OwsExceptionReport;
import org.n52.shetland.ogc.sos.request.DescribeSensorRequest;
import org.n52.svalbard.encode.exception.EncodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DescribeSensorPoxProcessor extends AbstractRequestProcessor {
	
	private static final Logger LOG = LoggerFactory.getLogger(DescribeSensorPoxProcessor.class);
	
	private DescribeSensorRequest request;

	public DescribeSensorPoxProcessor(DemoConfig demoConfig, HttpClientHandler httpClientHandler, DescribeSensorRequest request) {
		super(demoConfig, httpClientHandler);
		this.request = request;
	}

	@Override
	public Response process() {
		try {
			return query(createRequest(request));
		} catch (URISyntaxException | OwsExceptionReport | EncodingException e) {
			LOG.error("Error while processing DescribeSensor request!", e);
		}
		return null;
	}

}
