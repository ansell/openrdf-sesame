/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.trig;

import org.openrdf.rio.RDFWriterTest;

/**
 * @author Arjohn Kampman
 */
public class TriGWriterTest extends RDFWriterTest {

	public TriGWriterTest() {
		super(new TriGWriterFactory(), new TriGParserFactory());
	}
}
