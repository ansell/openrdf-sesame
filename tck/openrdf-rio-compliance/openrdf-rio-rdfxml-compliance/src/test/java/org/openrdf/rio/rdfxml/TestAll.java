/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.rdfxml;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 *
 * @author Herko ter Horst
 */
public class TestAll extends TestCase {

	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite("Test for org.openrdf.rio.rdfxml");
		//$JUnit-BEGIN$
		suite.addTest(RDFXMLParserTest.suite());
		suite.addTestSuite(RDFXMLWriterTest.class);
		suite.addTestSuite(RDFXMLPrettyWriterTest.class);
		//$JUnit-END$
		return suite;
	}

}
