/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
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
