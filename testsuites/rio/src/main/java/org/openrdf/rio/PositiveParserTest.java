/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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