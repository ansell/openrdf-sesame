/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql;

import java.util.List;

import junit.framework.Test;

import org.openrdf.query.QueryLanguage;

public class SeRQLQueryTest extends SeRQLQueryTestCase {

	public static Test suite() throws Exception {
		return SeRQLQueryTestCase.suite(new Factory() {
			public Test createTest(String name, String dataFile,
					List<String> graphNames, String queryFile,
					String resultFile, String entailment) {
				return new SeRQLQueryTest(name, dataFile, graphNames,
						queryFile, resultFile, entailment);
			}
		});
	}

	public SeRQLQueryTest(String name, String dataFile,
			List<String> graphNames, String queryFile, String resultFile,
			String entailment) {
		super(name, dataFile, graphNames, queryFile, resultFile, entailment);
	}

	@Override
	protected QueryLanguage getQueryLanguage() {
		return QueryLanguage.SERQL;
	}
}
