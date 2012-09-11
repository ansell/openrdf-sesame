// SES-1071 disabling rdbms-based tests
///*
// * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
// *
// * Licensed under the Aduna BSD-style license.
// */
//package org.openrdf.repository.sail.rdbms;
//
//import junit.framework.Test;
//
//import org.openrdf.query.Dataset;
//import org.openrdf.query.parser.sparql.ManifestTest;
//import org.openrdf.query.parser.sparql.SPARQLQueryTest;
//import org.openrdf.repository.Repository;
//import org.openrdf.repository.dataset.DatasetRepository;
//import org.openrdf.repository.sail.SailRepository;
//import org.openrdf.sail.rdbms.postgresql.PgSqlStore;
//
//public class PgSqlSPARQLQueryTest extends SPARQLQueryTest {
//
//	public static Test suite()
//		throws Exception
//	{
//		return ManifestTest.suite(new Factory() {
//
//			public PgSqlSPARQLQueryTest createSPARQLQueryTest(String testURI, String name, String queryFileURL,
//					String resultFileURL, Dataset dataSet, boolean laxCardinality)
//			{
//				return createSPARQLQueryTest(testURI, name, queryFileURL, resultFileURL, dataSet, laxCardinality,
//						false);
//			}
//
//			public PgSqlSPARQLQueryTest createSPARQLQueryTest(String testURI, String name, String queryFileURL,
//					String resultFileURL, Dataset dataSet, boolean laxCardinality, boolean checkOrder)
//			{
//				return new PgSqlSPARQLQueryTest(testURI, name, queryFileURL, resultFileURL, dataSet,
//						laxCardinality, checkOrder);
//			}
//		});
//	}
//
//	protected PgSqlSPARQLQueryTest(String testURI, String name, String queryFileURL, String resultFileURL,
//			Dataset dataSet, boolean laxCardinality, boolean checkOrder)
//	{
//		super(testURI, name, queryFileURL, resultFileURL, dataSet, laxCardinality, checkOrder);
//	}
//
//	protected Repository newRepository() {
//		PgSqlStore sail = new PgSqlStore("sesame_test");
//		sail.setUser("sesame");
//		sail.setPassword("opensesame");
//		return new DatasetRepository(new SailRepository(sail));
//	}
//}
