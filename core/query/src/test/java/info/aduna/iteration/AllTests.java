/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.iteration;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 * @author akam
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for info.aduna.iteration");
		//$JUnit-BEGIN$
		suite.addTestSuite(LimitIterationTest.class);
		suite.addTestSuite(EmptyIterationTest.class);
		suite.addTestSuite(OffsetIterationTest.class);
		suite.addTestSuite(ConvertingIterationTest.class);
		suite.addTestSuite(CloseableIteratorIterationTest.class);
		suite.addTestSuite(DelayedIterationTest.class);
		suite.addTestSuite(DistinctIterationTest.class);
		suite.addTestSuite(ExceptionConvertingIterationTest.class);
		suite.addTestSuite(FilterIterationTest.class);
		suite.addTestSuite(IntersectionIterationTest.class);
		suite.addTestSuite(DistinctIntersectionIterationTest.class);
		suite.addTestSuite(IteratorIterationTest.class);
		suite.addTestSuite(LockingIterationTest.class);
		suite.addTestSuite(LookAheadIterationTest.class);
		suite.addTestSuite(MinusIterationTest.class);
		suite.addTestSuite(DistinctMinusIterationTest.class);
		suite.addTestSuite(SingletonIterationTest.class);
		suite.addTestSuite(UnionIterationTest.class);
		//$JUnit-END$
		return suite;
	}
}
