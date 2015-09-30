/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class GraphQueryResultTest {

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
