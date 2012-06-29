/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import org.junit.Test;

import junit.framework.TestCase;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.DatasetImpl;

public abstract class SparqlDatasetTest extends TestCase {

	public String queryNoFrom = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
			+ "SELECT (COUNT(DISTINCT ?name) as ?c) \n" + " WHERE { ?x foaf:name  ?name . } ";

	public String queryWithFrom = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
			+ "SELECT (COUNT(DISTINCT ?name) as ?c) \n" + " FROM <http://example.org/graph1> "
			+ " WHERE { ?x foaf:name  ?name . } ";

	private Repository repository;

	private RepositoryConnection conn;

	private ValueFactory vf;

	private DatasetImpl dataset;

	private URI graph1 = new URIImpl("http://example.org/graph1");

	private URI george;

	private URI paul;

	private URI john;

	private URI ringo;

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

	@Override
	protected void setUp()
		throws Exception
	{
		repository = createRepository();
		vf = repository.getValueFactory();
		john = createUser("john", "John Lennon", "john@example.org");
		paul = createUser("paul", "Paul McCartney", "paul@example.org");
		george = createUser("george", "George Harrison", "george@example.org", graph1);
		ringo = createUser("ringo", "Ringo Starr", "ringo@example.org", graph1);
		conn = repository.getConnection();

		dataset = new DatasetImpl();
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

	@Override
	protected void tearDown()
		throws Exception
	{
		conn.close();
		conn = null;

		repository.shutDown();
		repository = null;
	}

	private URI createUser(String id, String name, String email, Resource... context)
		throws RepositoryException
	{
		RepositoryConnection conn = repository.getConnection();
		URI subj = vf.createURI("http://example.org/ns#", id);
		URI foafName = vf.createURI("http://xmlns.com/foaf/0.1/", "name");
		URI foafMbox = vf.createURI("http://xmlns.com/foaf/0.1/", "mbox");

		conn.add(subj, RDF.TYPE, vf.createURI("http://xmlns.com/foaf/0.1/", "Person"), context);
		conn.add(subj, foafName, vf.createLiteral(name), context);
		conn.add(subj, foafMbox, vf.createURI("mailto:", email), context);
		conn.close();

		return subj;
	}
}
