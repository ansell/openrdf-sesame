/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository.query;

import org.openrdf.rio.RDFFormat;

public class ConstructQueryInfo extends QueryInfo {

	private RDFFormat resultFormat = RDFFormat.RDFXML;

	public RDFFormat getResultFormat() {
		return resultFormat;
	}

	public void setResultFormat(RDFFormat resultFormat) {
		this.resultFormat = resultFormat;
	}
}
