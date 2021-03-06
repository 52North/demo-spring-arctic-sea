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

import org.apache.xmlbeans.XmlObject;
import org.n52.janmayen.http.MediaType;
import org.n52.janmayen.http.MediaTypes;

public class XmlPostRequest extends AbstractPostRequest<XmlObject> {

	private XmlObject content;

	public XmlPostRequest() {
	}

	public XmlPostRequest(XmlObject content) {
		this.content = content;
	}

	@Override
	public XmlPostRequest setContent(XmlObject content) {
		this.content = content;
		return this;
	}

	@Override
	public String getContent() {
		return content.xmlText();
	}

	@Override
	public boolean hasContent() {
		return content != null;
	}

	@Override
	public MediaType getContentType() {
		return MediaTypes.APPLICATION_XML;
	}

	@Override
	public String getPath() {
		return "";
	}

}
