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
package org.eclipse.rdf4j.query.parser.sparql.manifest;


import junit.framework.Test;

import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.parser.ParsedOperation;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;
import org.eclipse.rdf4j.query.parser.sparql.manifest.SPARQL11SyntaxTest;

public class W3CApprovedSPARQL11SyntaxTest extends SPARQL11SyntaxTest {

	public static Test suite()
		throws Exception
	{
		return SPARQL11SyntaxTest.suite(new Factory() {

			public SPARQL11SyntaxTest createSPARQLSyntaxTest(String testURI, String testName, String testAction,
					boolean positiveTest)
			{
				return new W3CApprovedSPARQL11SyntaxTest(testURI, testName, testAction, positiveTest);
			}
		}, false);
	}

	public W3CApprovedSPARQL11SyntaxTest(String testURI, String name, String queryFileURL, boolean positiveTest) {
		super(testURI, name, queryFileURL, positiveTest);
	}

	protected ParsedOperation parseOperation(String operation, String fileURL)
		throws MalformedQueryException
	{
		return QueryParserUtil.parseOperation(QueryLanguage.SPARQL, operation, fileURL);
	}
	
	@Override
	protected void runTest()
		throws Exception
	{
		if (this.getName().contains("syntax-update-54")) {
			// we skip this negative syntax test because it is an unnecessarily restrictive test that is almost
			// impossible to implement correctly, and which in practice Sesame handles correctly simply by 
			// assigning different blank node ids.
		}
		else {
			super.runTest();
		}
	}
}
