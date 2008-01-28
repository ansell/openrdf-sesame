/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.rdfxml;

import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriterFactory;

public class RDFXMLPrettyWriterTest extends RDFXMLWriterTest {

	public RDFXMLPrettyWriterTest() {
		super(new RDFXMLPrettyWriterFactory(), new RDFXMLParserFactory());
	}
}
