/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
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
		suite.addTestSuite(LookAheadIterationTest.class);
		suite.addTestSuite(MinusIterationTest.class);
		suite.addTestSuite(DistinctMinusIterationTest.class);
		suite.addTestSuite(SingletonIterationTest.class);
		suite.addTestSuite(UnionIterationTest.class);
		//$JUnit-END$
		return suite;
	}
}
