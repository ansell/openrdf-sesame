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

import java.io.InputStream;
import java.net.MalformedURLException;

import junit.framework.TestCase;

import org.openrdf.model.URI;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;

public class TurtleNegativeParserTest extends TestCase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private String inputURL;

	private String baseURL;

	private RDFParser targetParser;

	protected URI testUri;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public TurtleNegativeParserTest(URI testUri, String caseURI, String inputURL, String baseURL, RDFParser targetParser)
		throws MalformedURLException
	{
		super(caseURI);
		this.testUri = testUri;
		this.inputURL = inputURL;
		this.baseURL = baseURL;
		this.targetParser = targetParser;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected void runTest() {
		try {
			// Try parsing the input; this should result in an error being
			// reported.
			// targetParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

			targetParser.setRDFHandler(new StatementCollector());

			InputStream in = this.getClass().getResourceAsStream(inputURL);
			assertNotNull("Test resource was not found: inputURL=" + inputURL, in);
			targetParser.parse(in, baseURL);
			in.close();

			// System.err.println("Ignoring Turtle Negative Parser Test that does not report an expected error: "
			// + inputURL);
			fail("Parser parses erroneous data without reporting errors");
		}
		catch (RDFParseException e) {
			// This is expected as the input file is incorrect RDF
		}
		catch (Exception e) {
			fail("Error: " + e.getMessage());
		}
	}

} // end inner class TurtleNegativeParserTest