/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.lucene;

import java.io.IOException;

import junit.framework.TestCase;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;


/**
 * This test verifies why the LuceneSailTest.testGraphQuery() test fails
 * with LuceneSail using Sesame >= 2.1. LuceneSail bases on a feature that
 * was removed with Sesame 2.1. That feature is exploited here. This test
 * succeeds with Sesame < 2.1.
 * 
 * However, LuceneSail works fine, except such GraphQueries. If you need
 * them to work properly, use the sesame-2.0 branch of LuceneSail. See
 *   https://dev.nepomuk.semanticdesktop.org/wiki/LuceneSailGraphQueries
 *   https://dev.nepomuk.semanticdesktop.org/wiki/LuceneSailFlavors
 * 
 * This test, as well as the one in LuceneSailTest, is turned ON in the Sesame 2.0
 * branch of LuceneSail and is turned OFF in the LuceneSail trunk, which is Sesame >= 2.1.
 *
 * @author Enrico Minack
 */
public class GraphQueryTest extends TestCase {

	protected Repository repository;

	protected RepositoryConnection connection;

	@Override
	public void setUp()
		throws IOException, RepositoryException
	{
		// setup a memory sail
		MemoryStore sail = new MemoryStore();

		// create a Repository wrapping the sail
		repository = new SailRepository(sail);
		repository.initialize();

		connection = repository.getConnection();
	}
	
	@Override
	public void tearDown()
		throws RepositoryException
	{
		connection.close();
		repository.shutDown();
	}

	public void test() throws MalformedQueryException, RepositoryException, QueryEvaluationException {
//		StringBuilder query = new StringBuilder();
//		query.append("CONSTRUCT DISTINCT \n");
//		query.append("    {r1} <uri:p> {r2} , \n");
//		query.append("    {r1} <uri:p> {r3} \n");
//
//		GraphQuery tq = connection.prepareGraphQuery(QueryLanguage.SERQL, query.toString());
//		tq.setBinding("r1", new URIImpl("uri:one"));
//		tq.setBinding("r2", new URIImpl("uri:two"));
//		tq.setBinding("r3", new URIImpl("uri:three"));
//		GraphQueryResult result = tq.evaluate();
//		
//		int i=0;
//		while(result.hasNext()) {
//			Statement statement = result.next();
//			i++;
//		}
//		assertEquals(2, i);
	}
	
}
