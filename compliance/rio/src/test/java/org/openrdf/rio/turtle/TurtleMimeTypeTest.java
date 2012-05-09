/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.turtle;

import junit.framework.TestCase;

import org.openrdf.rio.RDFFormat;

/**
 * @author James Leigh
 */
public class TurtleMimeTypeTest extends TestCase {

	public void testTextTurtle() {
		assertEquals(RDFFormat.TURTLE, RDFFormat.forMIMEType("text/turtle"));
	}

	public void testTextTurtleUtf8() {
		assertEquals(RDFFormat.TURTLE, RDFFormat.forMIMEType("text/turtle;charset=UTF-8"));
	}

	public void testApplicationXTurtle() {
		assertEquals(RDFFormat.TURTLE, RDFFormat.forMIMEType("application/x-turtle"));
	}

}
