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
package org.openrdf.query.parser.sparql.manifest;

import junit.framework.Test;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.query.parser.sparql.manifest.SPARQL11SyntaxTest;

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

	protected void parseOperation(String operation, String fileURL)
		throws MalformedQueryException
	{
		QueryParserUtil.parseOperation(QueryLanguage.SPARQL, operation, fileURL);
	}
}
