/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.properties;

import java.beans.PropertyEditorSupport;

import org.openrdf.rio.RDFFormat;

public class RDFFormatPropertyEditor extends PropertyEditorSupport {

	@Override
	public String getAsText()
	{
		return ((RDFFormat)getValue()).getName();
	}

	@Override
	public void setAsText(String text)
		throws IllegalArgumentException
	{
		setValue(RDFFormat.valueOf(text));
	}
}