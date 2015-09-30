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
package org.eclipse.rdf4j.rio;

import java.io.InputStream;
import java.net.MalformedURLException;

import junit.framework.TestCase;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.eclipse.rdf4j.rio.helpers.ParseErrorCollector;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

public class NegativeParserTest extends TestCase {

	/*-----------*
	 * Variables *
	 *-----------*/
	private String inputURL;

	private String baseURL;

	private RDFParser targetParser;

	protected IRI testUri;

	protected FailureMode failureMode;

	protected boolean didIgnoreFailure;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public NegativeParserTest(IRI testUri, String caseURI, String inputURL, String baseURL,
			RDFParser targetParser, FailureMode failureMode)
		throws MalformedURLException
	{
		super(caseURI);
		this.testUri = testUri;
		this.inputURL = inputURL;
		this.baseURL = baseURL;
		this.targetParser = targetParser;
		this.failureMode = failureMode;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected void runTest() {
		ParseErrorCollector el = new ParseErrorCollector();
		try {
			// Try parsing the input; this should result in an error being
			// reported.
			// targetParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);
			
			targetParser.setRDFHandler(new StatementCollector());

			InputStream in = this.getClass().getResourceAsStream(inputURL);
			assertNotNull("Test resource was not found: inputURL=" + inputURL, in);
			
			System.err.println("test: " + inputURL);

			targetParser.setParseErrorListener(el);

			targetParser.parse(in, baseURL);
			in.close();

			if (failureMode.ignoreFailure()) {
				this.didIgnoreFailure = true;
				System.err.println("Ignoring Negative Parser Test that does not report an expected error: "
						+ inputURL);
			}
			else {
				this.didIgnoreFailure = false;
				fail("Parser parses erroneous data without reporting errors");
			}
		}
		catch (RDFParseException e) {
			// This is expected as the input file is incorrect RDF
		}
		catch (Exception e) {
			fail("Error: " + e.getMessage());
		}
	}

} // end inner class NegativeParserTest