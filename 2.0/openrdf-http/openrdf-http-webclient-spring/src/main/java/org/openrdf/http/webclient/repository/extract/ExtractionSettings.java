/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository.extract;

import org.openrdf.http.webclient.StatementSpecification;
import org.openrdf.rio.RDFFormat;

/**
 * @author Herko ter Horst
 */
public class ExtractionSettings extends StatementSpecification {

	private RDFFormat resultFormat = RDFFormat.RDFXML;

	public RDFFormat getResultFormat() {
		return resultFormat;
	}

	public void setResultFormat(RDFFormat resultFormat) {
		this.resultFormat = resultFormat;
	}
}
