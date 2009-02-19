/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms;

import junit.framework.Test;

import org.openrdf.query.Dataset;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.sparql.ManifestTest;
import org.openrdf.query.parser.sparql.SPARQLQueryTest;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.dataset.DatasetSail;
import org.openrdf.sail.rdbms.postgresql.PgSqlStore;
import org.openrdf.store.StoreException;

public class PgSqlCrossJoinTest extends SPARQLQueryTest {

	public static Test suite()
		throws Exception
	{
		return ManifestTest.suite(new Factory() {

			public PgSqlCrossJoinTest createSPARQLQueryTest(String testURI, String name, String queryFileURL,
					String resultFileURL, Dataset dataSet)
			{
				if (!name.contains("Join scope - 1"))
					return null;
				return new PgSqlCrossJoinTest(testURI, name, queryFileURL, resultFileURL, dataSet);
			}
		});
	}

	@Override
	protected Query prepareQuery(String queryString)
		throws StoreException
	{
		return con.prepareQuery(QueryLanguage.SPARQL, queryString, queryFileURL);
	}

	protected PgSqlCrossJoinTest(String testURI, String name, String queryFileURL, String resultFileURL,
			Dataset dataSet)
	{
		super(testURI, name, queryFileURL, resultFileURL, dataSet);
	}

	protected Repository newRepository() {
		PgSqlStore sail = new PgSqlStore("sesame_test");
		sail.setUser("sesame");
		sail.setPassword("opensesame");
		return new SailRepository(new DatasetSail(sail));
	}
}
