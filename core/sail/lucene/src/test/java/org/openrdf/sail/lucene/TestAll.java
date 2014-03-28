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
