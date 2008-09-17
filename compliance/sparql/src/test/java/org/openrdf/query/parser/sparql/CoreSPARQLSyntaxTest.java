/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

import junit.framework.Test;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.QueryParserUtil;

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
