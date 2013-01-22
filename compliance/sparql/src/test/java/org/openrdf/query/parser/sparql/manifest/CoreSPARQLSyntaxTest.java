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
import org.openrdf.query.parser.sparql.manifest.SPARQLSyntaxTest;

public class CoreSPARQLSyntaxTest extends SPARQLSyntaxTest {

	public static Test suite()
		throws Exception
	{
		return SPARQLSyntaxTest.suite(new Factory() {

			public SPARQLSyntaxTest createSPARQLSyntaxTest(String testURI, String testName, String testAction,
					boolean positiveTest)
			{
				return new CoreSPARQLSyntaxTest(testURI, testName, testAction, positiveTest);
			}
		});
	}

	public CoreSPARQLSyntaxTest(String testURI, String name, String queryFileURL, boolean positiveTest) {
		super(testURI, name, queryFileURL, positiveTest);
	}

	protected void parseQuery(String query, String queryFileURL)
		throws MalformedQueryException
	{
		QueryParserUtil.parseQuery(QueryLanguage.SPARQL, query, queryFileURL);
	}
}
