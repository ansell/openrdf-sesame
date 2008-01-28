/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.turtle;

import org.openrdf.rio.RDFWriterTest;

/**
 * @author Arjohn Kampman
 */
public class TurtleWriterTest extends RDFWriterTest {

	public TurtleWriterTest() {
		super(new TurtleWriterFactory(), new TurtleParserFactory());
	}
}
