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
package org.openrdf.repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;

import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.UnsupportedRDFormatException;

public abstract class TupleQueryResultTest extends TestCase {

	private Repository rep;

	private RepositoryConnection con;

	private String emptyResultQuery;

	private String singleResultQuery;

	private String multipleResultQuery;

	protected void setUp()
		throws Exception
	{
		rep = createRepository();
		con = rep.getConnection();

		buildQueries();
		addData();
	}

	@Override
	protected void tearDown()
		throws Exception
	{
		con.close();
		con = null;

		rep.shutDown();
		rep = null;

		super.tearDown();
	}

	protected Repository createRepository()
		throws Exception
	{
		Repository repository = newRepository();
		repository.initialize();
		RepositoryConnection con = repository.getConnection();
		con.clear();
		con.clearNamespaces();
		con.close();
		return repository;
	}

	protected abstract Repository newRepository()
		throws Exception;

	/*
	 * build some simple SeRQL queries to use for testing the result set object.
	 */
	private void buildQueries() {
		StringBuilder query = new StringBuilder();

		query.append("SELECT * ");
		query.append("FROM {X} P {Y} ");
		query.append("WHERE X != X ");

		emptyResultQuery = query.toString();

		query = new StringBuilder();

		query.append("SELECT DISTINCT P ");
		query.append("FROM {} dc:publisher {P} ");
		query.append("USING NAMESPACE ");
		query.append("   dc = <http://purl.org/dc/elements/1.1/>");

		singleResultQuery = query.toString();

		query = new StringBuilder();
		query.append("SELECT DISTINCT P, D ");
		query.append("FROM {} dc:publisher {P}; ");
		query.append("        dc:date {D} ");
		query.append("USING NAMESPACE ");
		query.append("   dc = <http://purl.org/dc/elements/1.1/>");

		multipleResultQuery = query.toString();
	}

	private void addData()
		throws IOException, UnsupportedRDFormatException, RDFParseException, RepositoryException
	{
		InputStream defaultGraph = TupleQueryResultTest.class.getResourceAsStream("/testcases/default-graph-1.ttl");
		try {
			con.add(defaultGraph, "", RDFFormat.TURTLE);
		}
		finally {
			defaultGraph.close();
		}
	}

	public void testGetBindingNames()
		throws Exception
	{
		TupleQueryResult result = con.prepareTupleQuery(QueryLanguage.SERQL, multipleResultQuery).evaluate();
		try {
			List<String> headers = result.getBindingNames();

			if (!headers.get(0).equals("P")) {
				fail("first header element should be 'P' but is '" + headers.get(0) + "'");
			}
			if (!headers.get(1).equals("D")) {
				fail("second header element should be 'D' but is '" + headers.get(1) + "'");
			}
		}
		finally {
			result.close();
		}
	}

	/*
	 * deprecated
	public void testIsDistinct()
		throws Exception
	{
		TupleQueryResult result = con.prepareTupleQuery(QueryLanguage.SERQL, emptyResultQuery).evaluate();

		try {
			if (result.isDistinct()) {
				fail("query result should not be distinct.");
			}
		}
		finally {
			result.close();
		}

		result = con.prepareTupleQuery(QueryLanguage.SERQL, singleResultQuery).evaluate();

		try {
			if (!result.isDistinct()) {
				fail("query result should be distinct.");
			}
		}
		finally {
			result.close();
		}
	}
	*/

	public void testIterator()
		throws Exception
	{
		TupleQueryResult result = con.prepareTupleQuery(QueryLanguage.SERQL, multipleResultQuery).evaluate();

		try {
			int count = 0;
			while (result.hasNext()) {
				result.next();
				count++;
			}

			if (count <= 1) {
				fail("query should have multiple results.");
			}
		}
		finally {
			result.close();
		}
	}

	public void testIsEmpty()
		throws Exception
	{
		TupleQueryResult result = con.prepareTupleQuery(QueryLanguage.SERQL, emptyResultQuery).evaluate();

		try {
			if (result.hasNext()) {
				fail("Query result should be empty");
			}
		}
		finally {
			result.close();
		}
	}
}
