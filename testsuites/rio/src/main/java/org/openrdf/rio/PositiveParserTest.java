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
package org.openrdf.rio;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.openrdf.model.Statement;
import org.openrdf.model.IRI;
import org.openrdf.model.util.Models;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.ParseErrorCollector;
import org.openrdf.rio.helpers.StatementCollector;

public class PositiveParserTest extends TestCase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private String inputURL;

	private String outputURL;

	private String baseURL;

	private RDFParser targetParser;

	private RDFParser ntriplesParser;

	protected IRI testUri;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public PositiveParserTest(IRI testUri, String testName, String inputURL, String outputURL,
			String baseURL, RDFParser targetParser, RDFParser ntriplesParser)
		throws MalformedURLException
	{
		super(testName);
		this.testUri = testUri;
		this.inputURL = inputURL;
		if (outputURL != null) {
			this.outputURL = outputURL;
		}
		this.baseURL = baseURL;
		this.targetParser = targetParser;
		this.ntriplesParser = ntriplesParser;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected void runTest()
		throws Exception
	{
		// Parse input data
		// targetParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

		Set<Statement> inputCollection = new LinkedHashSet<Statement>();
		StatementCollector inputCollector = new StatementCollector(inputCollection);
		targetParser.setRDFHandler(inputCollector);

		InputStream in = this.getClass().getResourceAsStream(inputURL);
		assertNotNull("Test resource was not found: inputURL=" + inputURL, in);

		System.err.println("test: " + inputURL);

		ParseErrorCollector el = new ParseErrorCollector();
		targetParser.setParseErrorListener(el);

		try {
			targetParser.parse(in, baseURL);
		}
		finally {
			in.close();

			if (!el.getFatalErrors().isEmpty()) {
				System.err.println("[Turtle] Input file had fatal parsing errors: ");
				System.err.println(el.getFatalErrors());
			}

			if (!el.getErrors().isEmpty()) {
				System.err.println("[Turtle] Input file had parsing errors: ");
				System.err.println(el.getErrors());
			}

			if (!el.getWarnings().isEmpty()) {
				System.err.println("[Turtle] Input file had parsing warnings: ");
				System.err.println(el.getWarnings());
			}
		}

		if (outputURL != null) {
			// Parse expected output data
			ntriplesParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

			Set<Statement> outputCollection = new LinkedHashSet<Statement>();
			StatementCollector outputCollector = new StatementCollector(outputCollection);
			ntriplesParser.setRDFHandler(outputCollector);

			in = this.getClass().getResourceAsStream(outputURL);
			try {
				ntriplesParser.parse(in, baseURL);
			}
			finally {
				in.close();
			}

			// Check equality of the two models
			if (!Models.isomorphic(inputCollection, outputCollection)) {
				System.err.println("===models not equal===");
				System.err.println("Expected: " + outputCollection);
				System.err.println("Actual  : " + inputCollection);
				System.err.println("======================");

				fail("models not equal");
			}
		}
	}

} // end inner class PositiveParserTest