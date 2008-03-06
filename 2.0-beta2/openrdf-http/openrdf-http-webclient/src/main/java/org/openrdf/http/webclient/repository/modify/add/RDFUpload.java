/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository.modify.add;

import java.io.IOException;
import java.io.InputStream;

import org.openrdf.rio.RDFFormat;

/**
 * @author Herko ter Horst
 */
public abstract class RDFUpload {

	private String baseUri;

	private RDFFormat format = RDFFormat.RDFXML;

	public RDFFormat getFormat() {
		return format;
	}

	public void setFormat(RDFFormat format) {
		this.format = format;
	}

	public String getBaseUri() {
		return baseUri;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public abstract InputStream getInputStream()
		throws IOException;
	
	public abstract String getI18n();
}
