/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail.memory;

import java.util.List;

import junit.framework.Test;

import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.serql.SeRQLQueryTestCase;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.memory.MemoryStore;

public class MemorySeRQLQueryTest extends SeRQLQueryTestCase {

	public static Test suite() throws Exception {
		return SeRQLQueryTestCase.suite(new Factory() {
			public Test createTest(String name, String dataFile,
					List<String> graphNames, String queryFile,
					String resultFile, String entailment) {
				return new MemorySeRQLQueryTest(name, dataFile, graphNames,
						queryFile, resultFile, entailment);
			}
		});
	}

	public MemorySeRQLQueryTest(String name, String dataFile,
			List<String> graphNames, String queryFile, String resultFile,
			String entailment) {
		super(name, dataFile, graphNames, queryFile, resultFile, entailment);
	}

	@Override
	protected QueryLanguage getQueryLanguage() {
		return QueryLanguage.SERQL;
	}

	@Override
	protected NotifyingSail newSail() {
		return new MemoryStore();
	}
}
