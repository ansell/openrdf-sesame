/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail.nativerdf;

import java.io.IOException;
import java.util.List;

import junit.framework.Test;

import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.serql.SeRQLQueryTestCase;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.nativerdf.NativeStore;

public class NativeSeRQLQueryTest extends SeRQLQueryTestCase {

	public static Test suite()
		throws Exception
	{
		return SeRQLQueryTestCase.suite(new Factory() {

			public Test createTest(String name, String dataFile, List<String> graphNames, String queryFile,
					String resultFile, String entailment)
			{
				return new NativeSeRQLQueryTest(name, dataFile, graphNames, queryFile, resultFile, entailment);
			}
		});
	}

	public NativeSeRQLQueryTest(String name, String dataFile, List<String> graphNames, String queryFile,
			String resultFile, String entailment)
	{
		super(name, dataFile, graphNames, queryFile, resultFile, entailment);
	}

	@Override
	protected QueryLanguage getQueryLanguage() {
		return QueryLanguage.SERQL;
	}

	@Override
	protected NotifyingSail newSail()
		throws IOException
	{
		NativeStore nativeStore = new NativeStore();
		nativeStore.setTripleIndexes("spoc");
		return nativeStore;
	}
}
