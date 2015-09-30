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

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class SparqlDatasetTest {

	public String queryNoFrom = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
			+ "SELECT (COUNT(DISTINCT ?name) as ?c) \n" + " WHERE { ?x foaf:name  ?name . } ";

	public String queryWithFrom = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
			+ "SELECT (COUNT(DISTINCT ?name) as ?c) \n" + " FROM <http://example.org/graph1> "
			+ " WHERE { ?x foaf:name  ?name . } ";

	private Repository repository;

	private RepositoryConnection conn;

	private ValueFactory vf;

	private SimpleDataset dataset;

	private IRI graph1;

	private IRI george;

	private IRI paul;

	private IRI john;

	private IRI ringo;

	@Test
	public void testNoFrom()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryNoFrom);
		TupleQueryResult result = query.evaluate();

		assertTrue(result.hasNext());

		if (result.hasNext()) {
			BindingSet bs = result.next();
			assertFalse(result.hasNext());

			Literal count = (Literal)bs.getValue("c");
			assertEquals(4, count.intValue());
		}
		result.close();

		query.setDataset(dataset);
		result = query.evaluate();

		assertTrue(result.hasNext());

		if (result.hasNext()) {
			BindingSet bs = result.next();
			assertFalse(result.hasNext());

			Literal count = (Literal)bs.getValue("c");
			assertEquals(2, count.intValue());
		}
		result.close();
	}

	@Test
	public void testWithFrom()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryWithFrom);
		TupleQueryResult result = query.evaluate();

		assertTrue(result.hasNext());

		if (result.hasNext()) {
			BindingSet bs = result.next();
			assertFalse(result.hasNext());

			Literal count = (Literal)bs.getValue("c");
			assertEquals(2, count.intValue());
		}
		result.close();

		query.setDataset(dataset);
		result = query.evaluate();

		assertTrue(result.hasNext());

		if (result.hasNext()) {
			BindingSet bs = result.next();
			assertFalse(result.hasNext());

			Literal count = (Literal)bs.getValue("c");
			assertEquals(2, count.intValue());
		}
		result.close();
	}

	@Before
	public void setUp()
		throws Exception
	{
		repository = createRepository();
		vf = repository.getValueFactory();
		graph1 = vf.createIRI("http://example.org/graph1");
		john = createUser("john", "John Lennon", "john@example.org");
		paul = createUser("paul", "Paul McCartney", "paul@example.org");
		george = createUser("george", "George Harrison", "george@example.org", graph1);
		ringo = createUser("ringo", "Ringo Starr", "ringo@example.org", graph1);
		conn = repository.getConnection();

		dataset = new SimpleDataset();
		dataset.addDefaultGraph(graph1);
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

	private IRI createUser(String id, String name, String email, Resource... context)
		throws RepositoryException
	{
		RepositoryConnection conn = repository.getConnection();
		IRI subj = vf.createIRI("http://example.org/ns#", id);
		IRI foafName = vf.createIRI("http://xmlns.com/foaf/0.1/", "name");
		IRI foafMbox = vf.createIRI("http://xmlns.com/foaf/0.1/", "mbox");

		conn.add(subj, RDF.TYPE, vf.createIRI("http://xmlns.com/foaf/0.1/", "Person"), context);
		conn.add(subj, foafName, vf.createLiteral(name), context);
		conn.add(subj, foafMbox, vf.createIRI("mailto:", email), context);
		conn.close();

		return subj;
	}
}
