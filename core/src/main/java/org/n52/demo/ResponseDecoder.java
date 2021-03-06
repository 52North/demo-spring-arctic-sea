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

import java.io.ByteArrayInputStream;

import org.apache.xmlbeans.XmlObject;
import org.n52.demo.web.Response;
import org.n52.shetland.ogc.ows.exception.CodedException;
import org.n52.shetland.ogc.ows.exception.NoApplicableCodeException;
import org.n52.svalbard.decode.Decoder;
import org.n52.svalbard.decode.DecoderKey;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.util.CodingHelper;

public class ResponseDecoder {

	private DemoConfig demoConfig;

	public ResponseDecoder(DemoConfig demoConfig) {
		this.demoConfig = demoConfig;
	}

	public Object decode(Response response) throws CodedException, DecodingException {
		XmlObject xml = parse(response.getEntity(), "UTF-8");
		DecoderKey key = getDecoderKey(xml);
		Decoder<Object, XmlObject> decoder = getDecoder(key);
		return decoder.decode(xml);
	}

	private DecoderKey getDecoderKey(XmlObject xml) {
		return CodingHelper.getDecoderKey(xml);
	}

	protected Decoder<Object, XmlObject> getDecoder(DecoderKey key) {
		return demoConfig.getDecoderRepository().getDecoder(key);
	}

	protected XmlObject parse(String xmlContent, String characterEncoding) throws CodedException {
		try (ByteArrayInputStream stream = new ByteArrayInputStream(xmlContent.getBytes(characterEncoding))) {
			return XmlObject.Factory.parse(stream);

		} catch (Exception e) {
			throw new NoApplicableCodeException().causedBy(e)
					.withMessage("An error occured when parsing the request! Message: %s", e.getMessage());
		}
	}
}
