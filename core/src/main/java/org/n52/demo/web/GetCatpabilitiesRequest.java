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

import java.util.Map;

import org.n52.shetland.ogc.ows.OWSConstants;
import org.n52.shetland.ogc.sos.SosConstants;

public class GetCatpabilitiesRequest extends AbstractGetRequest {

	private String acceptVersion;

	public GetCatpabilitiesRequest() {
	}

	public GetCatpabilitiesRequest withAcceptVersion(String acceptVersion) {
		this.acceptVersion = acceptVersion;
		return this;
	}

	private boolean isSetAcceptVersion() {
		return acceptVersion != null && !acceptVersion.isEmpty();
	}

	@Override
	public Map<String, String> getQueryParameters() {
		Map<String, String> map = createMap();
		map.put("service", SosConstants.SOS);
		map.put("request", SosConstants.Operations.GetCapabilities.name());
		if (isSetAcceptVersion()) {
			map.put(OWSConstants.GetCapabilitiesParams.AcceptVersions.name(), acceptVersion);
		}
		return map;
	}

	@Override
	public String getPath() {
		return "";
	}

}
