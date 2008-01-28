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
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

public class ValuePropertyEditor extends PropertyEditorSupport {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	private ValueFactory valueFactory;

	public ValuePropertyEditor(ValueFactory valueFactory) {
		this.valueFactory = valueFactory;
	}

	@Override
	public String getAsText()
	{
		String result = null;

		Value value = (Value)getValue();
		if (value != null) {
			result = Protocol.encodeValue(value);
			logger.debug("Getting value as " + result);
		}

		return result;
	}

	@Override
	public void setAsText(String text)
		throws IllegalArgumentException
	{
		if(!"".equals(text)) {
			Value value = Protocol.decodeValue(text, valueFactory);
			logger.debug("Setting value to " + value);
			setValue(value);
		}
		else {
			setValue(null);
		}
	}

}