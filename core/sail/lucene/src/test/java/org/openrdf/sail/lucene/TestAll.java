/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.lucene;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * 
 * @author grimnes
 */
public class TestAll extends TestCase {

	static public Test suite() {
		TestSuite suite = new TestSuite("Test for openrdf-lucenesail");
		suite.addTest(new TestSuite(GraphQueryTest.class));
		suite.addTest(new TestSuite(LuceneIndexTest.class));
		suite.addTest(new TestSuite(LuceneSailTest.class));
		suite.addTest(new TestSuite(LuceneSailIndexedPropertiesTest.class));
		suite.addTest(new TestSuite(QuerySpecBuilderTest.class));
		return suite;
	}

}
