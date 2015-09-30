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

import java.util.Arrays;
import java.util.List;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class SparqlOrderByTest {

	private String query1 = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n" + "SELECT ?name\n"
			+ "WHERE { ?x foaf:name ?name }\n" + "ORDER BY ?name\n";

	private String query2 = "PREFIX     :    <http://example.org/ns#>\n"
			+ "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n"
			+ "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n" + "SELECT ?name\n"
			+ "WHERE { ?x foaf:name ?name ; :empId ?emp }\n" + "ORDER BY DESC(?emp)\n";

	private String query3 = "PREFIX     :    <http://example.org/ns#>\n"
			+ "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n" + "SELECT ?name\n"
			+ "WHERE { ?x foaf:name ?name ; :empId ?emp }\n" + "ORDER BY ?name DESC(?emp)\n";

	private Repository repository;

	private RepositoryConnection conn;

	@Test
	public void testQuery1()
		throws Exception
	{
		assertTrue("James Leigh".compareTo("James Leigh Hunt") < 0);
		assertResult(query1, Arrays.asList("James Leigh", "James Leigh", "James Leigh Hunt", "Megan Leigh"));
	}

	@Test
	public void testQuery2()
		throws Exception
	{
		assertResult(query2, Arrays.asList("Megan Leigh", "James Leigh", "James Leigh Hunt", "James Leigh"));
	}

	@Test
	public void testQuery3()
		throws Exception
	{
		assertResult(query3, Arrays.asList("James Leigh", "James Leigh", "James Leigh Hunt", "Megan Leigh"));
	}

	@Before
	public void setUp()
		throws Exception
	{
		repository = createRepository();
		createEmployee("james", "James Leigh", 123);
		createEmployee("jim", "James Leigh", 244);
		createEmployee("megan", "Megan Leigh", 1234);
		createEmployee("hunt", "James Leigh Hunt", 243);
		conn = repository.getConnection();
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

	@After
	public void tearDown()
		throws Exception
	{
		conn.close();
		conn = null;

		repository.shutDown();
		repository = null;
	}

	private void createEmployee(String id, String name, int empId)
		throws RepositoryException
	{
		ValueFactory vf = repository.getValueFactory();
		String foafName = "http://xmlns.com/foaf/0.1/name";
		String exEmpId = "http://example.org/ns#empId";
		RepositoryConnection conn = repository.getConnection();
		conn.add(vf.createIRI("http://example.org/ns#" + id), vf.createIRI(foafName), vf.createLiteral(name));
		conn.add(vf.createIRI("http://example.org/ns#" + id), vf.createIRI(exEmpId), vf.createLiteral(empId));
		conn.close();
	}

	private void assertResult(String queryStr, List<String> names)
		throws RepositoryException, MalformedQueryException, QueryEvaluationException
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryStr);
		TupleQueryResult result = query.evaluate();
		for (String name : names) {
			Value value = result.next().getValue("name");
			assertEquals(name, ((Literal)value).getLabel());
		}
		assertFalse(result.hasNext());
		result.close();
	}
}
