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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class TupleQueryResultTest {

	private Repository rep;

	private RepositoryConnection con;

	private String emptyResultQuery;

	private String singleResultQuery;

	private String multipleResultQuery;

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

	@Test
	public void testGetBindingNames()
		throws Exception
	{
		TupleQueryResult result = con.prepareTupleQuery(QueryLanguage.SERQL, multipleResultQuery).evaluate();
		try {
			List<String> headers = result.getBindingNames();

			assertThat("first header element", headers.get(0), is("P"));
			assertThat("second header element", headers.get(1), is("D"));
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

	@Test
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

			assertTrue("query should have multiple results.", count > 1);
		}
		finally {
			result.close();
		}
	}

	@Test
	public void testIsEmpty()
		throws Exception
	{
		TupleQueryResult result = con.prepareTupleQuery(QueryLanguage.SERQL, emptyResultQuery).evaluate();

		try {
			assertFalse("Query result should be empty", result.hasNext());
		}
		finally {
			result.close();
		}
	}
}
