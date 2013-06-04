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

import org.openrdf.model.Model;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResults;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.UnsupportedRDFormatException;

public abstract class GraphQueryResultTest extends TestCase {

	private Repository rep;

	private RepositoryConnection con;

	private String emptyDescribeQuery;

	private String singleDescribeQuery;

	private String multipleDescribeQuery;

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
		try {
			con.clear();
			con.clearNamespaces();
		}
		finally {
			con.close();
		}
		return repository;
	}

	protected abstract Repository newRepository()
		throws Exception;

	/*
	 * build some simple SPARQL queries to use for testing the result set object.
	 */
	private void buildQueries() {
		StringBuilder query = new StringBuilder();

		query.append("DESCRIBE <urn:test:non-existent-uri>");

		emptyDescribeQuery = query.toString();

		query = new StringBuilder();

		query.append("DESCRIBE <" + OWL.THING.stringValue() + ">");

		singleDescribeQuery = query.toString();

		query = new StringBuilder();

		query.append("DESCRIBE <" + OWL.CLASS.stringValue() + ">");

		multipleDescribeQuery = query.toString();
	}

	private void addData()
		throws IOException, UnsupportedRDFormatException, RDFParseException, RepositoryException
	{
		InputStream defaultGraph = GraphQueryResultTest.class.getResourceAsStream("/testcases/graph3.ttl");
		try {
			con.add(defaultGraph, "", RDFFormat.TURTLE);
		}
		finally {
			defaultGraph.close();
		}
	}

	public void testDescribeEmpty()
		throws Exception
	{
		GraphQueryResult result = con.prepareGraphQuery(QueryLanguage.SPARQL, emptyDescribeQuery).evaluate();
		assertFalse("Query result should be empty", result.hasNext());
		
		Model model = QueryResults.asModel(result);
		assertTrue("Query result should be empty", model.isEmpty());
	}

	public void testDescribeSingle()
		throws Exception
	{
		GraphQueryResult result = con.prepareGraphQuery(QueryLanguage.SPARQL, singleDescribeQuery).evaluate();
		assertTrue("Query result should not be empty", result.hasNext());
		
		Model model = QueryResults.asModel(result);
		assertFalse("Query result should not be empty", model.isEmpty());
		assertEquals(1, model.size());
	}

	public void testDescribeMultiple()
		throws Exception
	{
		GraphQueryResult result = con.prepareGraphQuery(QueryLanguage.SPARQL, multipleDescribeQuery).evaluate();
		assertTrue("Query result should not be empty", result.hasNext());
		
		Model model = QueryResults.asModel(result);
		assertFalse("Query result should not be empty", model.isEmpty());
		assertEquals(4, model.size());
	}

}
