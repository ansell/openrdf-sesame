/*
 * Copyright (c) 2011 3 Round Stones Inc., Some rights reserved.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;

public abstract class SparqlAggregatesTest extends TestCase {

	public String selectNameMbox = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + "SELECT ?name ?mbox\n"
			+ " WHERE { ?x foaf:name  ?name; foaf:mbox  ?mbox }";

	public String concatMbox = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
			+ "SELECT ?name (group_concat(?mbox) AS ?mbox)\n"
			+ " WHERE { ?x foaf:name  ?name; foaf:mbox  ?mbox } GROUP BY ?name";

	public String concatOptionalMbox = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
		+ "SELECT ?name (group_concat(?mbox) AS ?mbox)\n"
		+ " WHERE { ?x foaf:name  ?name OPTIONAL { ?x foaf:mbox  ?mbox } } GROUP BY ?name";

	public String countMbox = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
			+ "SELECT ?name (count(?mbox) AS ?mbox)\n"
			+ " WHERE { ?x foaf:name  ?name; foaf:mbox  ?mbox } GROUP BY ?name";

	public String countOptionalMbox = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
		+ "SELECT ?name (count(?mb) AS ?mbox)\n"
		+ " WHERE { ?x foaf:name  ?name OPTIONAL { ?x foaf:mbox  ?mb } } GROUP BY ?name";

	private Repository repository;

	private RepositoryConnection conn;

	private ValueFactory vf;

	public void testSelect()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, selectNameMbox);
		TupleQueryResult result = query.evaluate();
		assertTrue(result.hasNext());
		result.next();
		result.next();
		assertFalse(result.hasNext());
		result.close();
	}

	public void testConcat()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, concatMbox);
		TupleQueryResult result = query.evaluate();
		assertTrue(result.hasNext());
		assertNotNull(result.next().getValue("mbox"));
		assertNotNull(result.next().getValue("mbox"));
		assertFalse(result.hasNext());
		result.close();
	}

	public void testConcatOptional()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, concatOptionalMbox);
		TupleQueryResult result = query.evaluate();
		assertTrue(result.hasNext());
		result.next();
		result.next();
		result.next();
		assertFalse(result.hasNext());
		result.close();
	}

	public void testCount()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, countMbox);
		TupleQueryResult result = query.evaluate();
		assertTrue(result.hasNext());
		assertEquals("1", result.next().getValue("mbox").stringValue());
		assertEquals("1", result.next().getValue("mbox").stringValue());
		assertFalse(result.hasNext());
		result.close();
	}

	public void testCountOptional()
		throws Exception
	{
		Set<String> zeroOr1 = new HashSet<String>();
		zeroOr1.add("0");
		zeroOr1.add("1");
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, countOptionalMbox);
		TupleQueryResult result = query.evaluate();
		assertTrue(result.hasNext());
		assertTrue(zeroOr1.contains(result.next().getValue("mbox").stringValue()));
		assertTrue(zeroOr1.contains(result.next().getValue("mbox").stringValue()));
		assertTrue(zeroOr1.contains(result.next().getValue("mbox").stringValue()));
		assertFalse(result.hasNext());
		result.close();
	}

	@Override
	protected void setUp()
		throws Exception
	{
		repository = createRepository();
		vf = repository.getValueFactory();
		createUser("james", "James Leigh", "james@leigh");
		createUser("megan", "Megan Leigh", "megan@leigh");
		createUser("hunt", "James Leigh Hunt", null);
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

	@Override
	protected void tearDown()
		throws Exception
	{
		conn.close();
		conn = null;

		repository.shutDown();
		repository = null;
	}

	private void createUser(String id, String name, String email)
		throws RepositoryException
	{
		RepositoryConnection conn = repository.getConnection();
		URI subj = vf.createURI("http://example.org/ns#", id);
		URI foafName = vf.createURI("http://xmlns.com/foaf/0.1/", "name");
		URI foafMbox = vf.createURI("http://xmlns.com/foaf/0.1/", "mbox");
		conn.add(subj, foafName, vf.createLiteral(name));
		if (email != null) {
			conn.add(subj, foafMbox, vf.createURI("mailto:", email));
		}
		conn.close();
	}
}
