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

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.Timeout;

import org.openrdf.model.Model;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResults;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.UnsupportedRDFormatException;

public abstract class GraphQueryResultTest {

	@Rule
	public TemporaryFolder tempDir = new TemporaryFolder();

	/**
	 * Timeout all individual tests after 1 minute.
	 */
	@Rule
	public Timeout to = new Timeout(60000);

	private Repository rep;

	private RepositoryConnection con;

	private String emptyDescribeQuery;

	private String singleDescribeQuery;

	private String multipleDescribeQuery;

	private String emptyConstructQuery;

	private String singleConstructQuery;

	private String multipleConstructQuery;

	@Before
	public void setUp()
		throws Exception
	{
		rep = createRepository();
		con = rep.getConnection();

		buildQueries();
		addData();
	}

	@After
	public void tearDown()
		throws Exception
	{
		con.close();
		con = null;

		rep.shutDown();
		rep = null;
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
		emptyDescribeQuery = "DESCRIBE <urn:test:non-existent-uri>";
		singleDescribeQuery = "DESCRIBE <" + OWL.THING.stringValue() + ">";
		multipleDescribeQuery = "DESCRIBE <" + OWL.CLASS.stringValue() + ">";

		emptyConstructQuery = "CONSTRUCT { <urn:test:non-existent-uri> ?p ?o . } WHERE { <urn:test:non-existent-uri> ?p ?o . }";
		singleConstructQuery = "CONSTRUCT { ?s ?p <" + OWL.THING.stringValue() + "> . } WHERE { ?s ?p <"
				+ OWL.THING.stringValue() + "> . }";
		multipleConstructQuery = "CONSTRUCT { ?s ?p <" + OWL.CLASS.stringValue() + "> . } WHERE { ?s ?p <"
				+ OWL.CLASS.stringValue() + "> . }";
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

	@Test
	public void testDescribeEmpty()
		throws Exception
	{
		GraphQueryResult result = con.prepareGraphQuery(QueryLanguage.SPARQL, emptyDescribeQuery).evaluate();
		assertFalse("Query result should be empty", result.hasNext());

		Model model = QueryResults.asModel(result);
		assertTrue("Query result should be empty", model.isEmpty());
	}

	@Test
	public void testDescribeSingle()
		throws Exception
	{
		GraphQueryResult result = con.prepareGraphQuery(QueryLanguage.SPARQL, singleDescribeQuery).evaluate();
		assertTrue("Query result should not be empty", result.hasNext());

		Model model = QueryResults.asModel(result);
		assertFalse("Query result should not be empty", model.isEmpty());
		assertEquals(1, model.size());
	}

	@Test
	public void testDescribeMultiple()
		throws Exception
	{
		GraphQueryResult result = con.prepareGraphQuery(QueryLanguage.SPARQL, multipleDescribeQuery).evaluate();
		assertTrue("Query result should not be empty", result.hasNext());

		Model model = QueryResults.asModel(result);
		assertFalse("Query result should not be empty", model.isEmpty());
		assertEquals(4, model.size());
	}

	@Test
	public void testConstructEmpty()
		throws Exception
	{
		GraphQueryResult result = con.prepareGraphQuery(QueryLanguage.SPARQL, emptyConstructQuery).evaluate();
		assertFalse("Query result should be empty", result.hasNext());

		Model model = QueryResults.asModel(result);
		assertTrue("Query result should be empty", model.isEmpty());
	}

	@Test
	public void testConstructSingle()
		throws Exception
	{
		GraphQueryResult result = con.prepareGraphQuery(QueryLanguage.SPARQL, singleConstructQuery).evaluate();
		assertTrue("Query result should not be empty", result.hasNext());

		Model model = QueryResults.asModel(result);
		assertFalse("Query result should not be empty", model.isEmpty());
		assertEquals(1, model.size());
	}

	@Test
	public void testConstructMultiple()
		throws Exception
	{
		GraphQueryResult result = con.prepareGraphQuery(QueryLanguage.SPARQL, multipleConstructQuery).evaluate();
		assertTrue("Query result should not be empty", result.hasNext());

		Model model = QueryResults.asModel(result);
		assertFalse("Query result should not be empty", model.isEmpty());
		assertEquals(4, model.size());
	}

}
