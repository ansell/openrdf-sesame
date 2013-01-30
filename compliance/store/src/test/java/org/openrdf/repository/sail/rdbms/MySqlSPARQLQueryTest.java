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
// SES-1071 disabling rdbms-based tests
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
//import org.openrdf.sail.rdbms.mysql.MySqlStore;
//
//public class MySqlSPARQLQueryTest extends SPARQLQueryTest {
//
//	public static Test suite()
//		throws Exception
//	{
//		return ManifestTest.suite(new Factory() {
//
//			public MySqlSPARQLQueryTest createSPARQLQueryTest(String testURI, String name, String queryFileURL,
//					String resultFileURL, Dataset dataSet, boolean laxCardinality)
//			{
//				return createSPARQLQueryTest(testURI, name, queryFileURL, resultFileURL, dataSet, laxCardinality,
//						false);
//			}
//
//			public MySqlSPARQLQueryTest createSPARQLQueryTest(String testURI, String name, String queryFileURL,
//					String resultFileURL, Dataset dataSet, boolean laxCardinality, boolean checkOrder)
//			{
//				return new MySqlSPARQLQueryTest(testURI, name, queryFileURL, resultFileURL, dataSet,
//						laxCardinality, checkOrder);
//			}
//		});
//	}
//
//	protected MySqlSPARQLQueryTest(String testURI, String name, String queryFileURL, String resultFileURL,
//			Dataset dataSet, boolean laxCardinality, boolean checkOrder)
//	{
//		super(testURI, name, queryFileURL, resultFileURL, dataSet, laxCardinality, checkOrder);
//	}
//
//
//	protected Repository newRepository() {
//		MySqlStore sail = new MySqlStore("sesame_test");
//		sail.setUser("sesame");
//		sail.setPassword("opensesame");
//		return new DatasetRepository(new SailRepository(sail));
//	}
//}
