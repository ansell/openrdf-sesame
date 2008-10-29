/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import static org.openrdf.query.QueryLanguage.SPARQL;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.openrdf.model.Value;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

public class OptionalOrderBy extends TestCase {

	private static final String PREFIX = "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
			+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n";

	private Repository repository;

	public void testOptionalOrderBy()
		throws Exception
	{
		String query = PREFIX + "SELECT ?a ?label WHERE {?a rdf:type owl:Thing ."
				+ "OPTIONAL {?a rdfs:label ?label } } ORDER BY ?label";
		List<String> labels = evaluate("label", query);
		labels.removeAll(Collections.singleton(null));
		assertFalse(labels.isEmpty());
		TreeSet<String> sorted = new TreeSet<String>(labels);
		assertEquals(Arrays.asList(sorted.toArray(new String[0])), labels);
	}

	private List<String> evaluate(String name, String query)
		throws Exception
	{
		List<String> labels = new ArrayList<String>();
		RepositoryConnection con = repository.getConnection();
		try {
			assertTrue(con.size() > 0);
			TupleQuery qry = con.prepareTupleQuery(SPARQL, query);
			TupleQueryResult result = qry.evaluate();
			try {
				while (result.hasNext()) {
					Value value = result.next().getValue(name);
					if (value == null) {
						labels.add(null);
					} else {
						labels.add(value.stringValue());
					}
				}
			}
			finally {
				result.close();
			}
		}
		finally {
			con.close();
		}
		return labels;
	}

	@Override
	protected void setUp()
		throws Exception
	{
		repository = createRepository();
		repository.initialize();
		load("testcases/optional-order-by.rdf");
	}

	private void load(String resource)
		throws Exception
	{
		RepositoryConnection con = repository.getConnection();
		try {
			ClassLoader cl = getClass().getClassLoader();
			URL url = cl.getResource(resource);
			con.add(url, "", RDFFormat.forFileName(resource));
		} finally {
			con.close();
		}
	}

	@Override
	protected void tearDown()
		throws Exception
	{
		repository.shutDown();
	}

	/**
	 * Gets an (uninitialized) instance of the repository that should be tested.
	 * 
	 * @return an uninitialized repository.
	 */
	protected Repository createRepository()
		throws Exception
	{
		return new SailRepository(new MemoryStore());
	}
}
