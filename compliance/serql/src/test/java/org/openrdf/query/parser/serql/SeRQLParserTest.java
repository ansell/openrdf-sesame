/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql;

import junit.framework.Test;

import org.openrdf.model.Value;
import org.openrdf.query.parser.QueryParser;

public class SeRQLParserTest extends SeRQLParserTestCase {

	public static Test suite()
		throws Exception
	{
		return SeRQLParserTestCase.suite(new Factory() {

			public Test createTest(String name, String queryFile, Value result) {
				return new SeRQLParserTest(name, queryFile, result);
			}
		});
	}

	public SeRQLParserTest(String name, String queryFile, Value result) {
		super(name, queryFile, result);
	}

	@Override
	protected QueryParser createParser() {
		return new SeRQLParser();
	}
}
