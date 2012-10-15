/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.nquads;

/**
 * JUnit test for the N-Quads writer.
 *
 * @author Peter Ansell
 */
public class NQuadsWriterTest extends NQuadsWriterTestCase {

	public NQuadsWriterTest() {
		super(new NQuadsWriterFactory(), new NQuadsParserFactory());
	}

}
