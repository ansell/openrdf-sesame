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
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;

public class UriPropertyEditor extends PropertyEditorSupport {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	private ValueFactory valueFactory;

	public UriPropertyEditor(ValueFactory valueFactory) {
		this.valueFactory = valueFactory;
	}

	@Override
	public String getAsText()
	{
		String result = null;

		URI uri = (URI)getValue();
		if (uri != null) {
			result = Protocol.encodeValue(uri);
			logger.debug("Getting uri as " + result);
		}

		return result;
	}

	@Override
	public void setAsText(String text)
		throws IllegalArgumentException
	{
		if (!"".equals(text)) {
			URI uri = Protocol.decodeURI(text, valueFactory);
			logger.debug("Setting uri to " + uri);
			setValue(uri);
		}
		else {
			setValue(null);
		}
	}

}