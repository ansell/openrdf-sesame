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
package org.openrdf.rio.turtle;

import junit.framework.Test;

import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.TurtleParserSettings;
import org.openrdf.rio.ntriples.NTriplesParser;

/**
 * JUnit test for the Turtle parser that uses the tests that are available <a
 * href="https://dvcs.w3.org/hg/rdf/file/09a9da374a9f/rdf-turtle/">online</a>.
 */
public class TurtleParserTest extends TurtleParserTestCase {

	public static Test suite()
		throws Exception
	{
		return new TurtleParserTest().createTestSuite();
	}

	@Override
	protected RDFParser createTurtleParser() {
		RDFParser result = new TurtleParser();
		return result;
	}

	@Override
	protected RDFParser createNTriplesParser() {
		return new NTriplesParser();
	}
}
