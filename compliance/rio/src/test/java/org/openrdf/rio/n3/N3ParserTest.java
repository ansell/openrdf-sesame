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
package org.openrdf.rio.n3;

import org.junit.Ignore;

import junit.framework.Test;

import org.openrdf.rio.RDFParser;
import org.openrdf.rio.turtle.TurtleParser;

/**
 * JUnit test for the N3 parser that uses the tests that are available <a
 * href="http://www.w3.org/2000/10/swap/test/n3parser.tests">online</a>.
 */
@Ignore
public class N3ParserTest extends N3ParserTestCase {

	public static Test suite()
		throws Exception
	{
		return new N3ParserTest().createTestSuite();
	}

	@Override
	protected RDFParser createRDFParser() {
		return new TurtleParser();
	}
}
