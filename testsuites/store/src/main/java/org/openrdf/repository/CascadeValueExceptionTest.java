package org.openrdf.repository;

import junit.framework.TestCase;

import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;

public abstract class CascadeValueExceptionTest extends TestCase {
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
		repository = createRepository();
		conn = repository.getConnection();
		conn.add(RDF.NIL, RDF.TYPE, RDF.LIST);
	}

	protected Repository createRepository() throws Exception {
		Repository repository = newRepository();
		repository.initialize();
		return repository;
	}

	protected abstract Repository newRepository() throws Exception;

	@Override
	protected void tearDown() throws Exception {
		conn.close();
		repository.shutDown();
	}

}
