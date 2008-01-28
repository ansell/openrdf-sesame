/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.properties;

import java.beans.PropertyEditorSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;

public class ResourcePropertyEditor extends PropertyEditorSupport {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	private ValueFactory valueFactory;

	public ResourcePropertyEditor(ValueFactory valueFactory) {
		this.valueFactory = valueFactory;
	}

	@Override
	public String getAsText()
	{
		String result = null;

		Resource resource = (Resource)getValue();
		if (resource != null) {
			result = Protocol.encodeValue(resource);
			logger.debug("Getting resource as " + result);
		}

		return result;
	}

	@Override
	public void setAsText(String text)
		throws IllegalArgumentException
	{
		if (!"".equals(text)) {
			Resource result = Protocol.decodeResource(text, valueFactory);
			logger.debug("Setting resource to " + result);
			setValue(result);
		}
		else {
			setValue(null);
		}
	}

}