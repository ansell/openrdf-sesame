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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;

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
