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
package org.openrdf.rio.ntriples;

import java.io.InputStream;

import junit.framework.TestCase;

import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.RDFHandlerBase;

/**
 * JUnit test for the N-Triples parser.
 * 
 * @author Arjohn Kampman
 */
public abstract class NTriplesParserTestCase extends TestCase {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static String NTRIPLES_TEST_URL = "http://www.w3.org/2000/10/rdf-tests/rdfcore/ntriples/test.nt";

	private static String NTRIPLES_TEST_FILE = "/testcases/ntriples/test.nt";

	/*---------*
	 * Methods *
	 *---------*/

	public void testNTriplesFile()
		throws Exception
	{
		RDFParser turtleParser = createRDFParser();
		turtleParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);
		turtleParser.setRDFHandler(new RDFHandlerBase());

		InputStream in = NTriplesParser.class.getResourceAsStream(NTRIPLES_TEST_FILE);
		try {
			turtleParser.parse(in, NTRIPLES_TEST_URL);
		}
		catch (RDFParseException e) {
			fail("Failed to parse N-Triples test document: " + e.getMessage());
		}
		finally {
			in.close();
		}
	}

	protected abstract RDFParser createRDFParser();
}
