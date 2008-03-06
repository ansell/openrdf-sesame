package org.openrdf.query.algebra.evaluation.impl;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.openrdf.query.algebra.evaluation.impl");
		//$JUnit-BEGIN$
		suite.addTestSuite(SparqlOrderByTest.class);
		suite.addTestSuite(SparqlRegexTest.class);
		//$JUnit-END$
		return suite;
	}

}
