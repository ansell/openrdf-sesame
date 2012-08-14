/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.binary;

import org.openrdf.rio.RDFWriterTest;

/**
 * @author Arjohn Kampman
 */
public class BinaryRDFWriterTest extends RDFWriterTest {

	public BinaryRDFWriterTest() {
		super(new BinaryRDFWriterFactory(), new BinaryRDFParserFactory());
	}
}
