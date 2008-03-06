package org.openrdf.query.algebra.evaluation.impl;

import junit.framework.TestCase;

import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

public class CascadeValueExceptionTest extends TestCase {
	private static String queryStr1 = "SELECT *\n"
			+ "WHERE {\n"
			+ "  ?s ?p ?o FILTER( !(\"2002\" < \"2007\"^^<http://www.w3.org/2001/XMLSchema#gYear>))\n"
			+ "}";

	private static String queryStr2 = "SELECT *\n"
			+ "WHERE {\n"
			+ "  ?s ?p ?o FILTER( !(\"2002\" = \"2007\"^^<http://www.w3.org/2001/XMLSchema#gYear>))\n"
			+ "}";

	private RepositoryConnection conn;

	private Repository repository;

	public void testValueExceptionLessThan() throws Exception {
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL,
				queryStr1);
		TupleQueryResult evaluate = query.evaluate();
		try {
			assertFalse(evaluate.hasNext());
		} finally {
			evaluate.close();
		}
	}

	public void testValueExceptionEqual() throws Exception {
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL,
				queryStr2);
		TupleQueryResult evaluate = query.evaluate();
		try {
			assertFalse(evaluate.hasNext());
		} finally {
			evaluate.close();
		}
	}

	@Override
	protected void setUp() throws Exception {
		repository = new SailRepository(new MemoryStore());
		repository.initialize();
		conn = repository.getConnection();
		conn.add(RDF.NIL, RDF.TYPE, RDF.LIST);
	}

	@Override
	protected void tearDown() throws Exception {
		conn.close();
		repository.shutDown();
	}

}
