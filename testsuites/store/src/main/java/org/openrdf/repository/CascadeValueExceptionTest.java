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

import junit.framework.TestCase;

import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;

public abstract class CascadeValueExceptionTest extends TestCase {

	private static String queryStr1 = "SELECT * WHERE { ?s ?p ?o FILTER( !(\"2002\" < \"2007\"^^<http://www.w3.org/2001/XMLSchema#gYear>))}";

	private static String queryStr2 = "SELECT * WHERE { ?s ?p ?o FILTER( !(\"2002\" = \"2007\"^^<http://www.w3.org/2001/XMLSchema#gYear>))}";

	private RepositoryConnection conn;

	private Repository repository;

	public void testValueExceptionLessThan()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryStr1);
		TupleQueryResult evaluate = query.evaluate();
		try {
			assertFalse(evaluate.hasNext());
		}
		finally {
			evaluate.close();
		}
	}

	public void testValueExceptionEqual()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryStr2);
		TupleQueryResult evaluate = query.evaluate();
		try {
			assertFalse(evaluate.hasNext());
		}
		finally {
			evaluate.close();
		}
	}

	@Override
	protected void setUp()
		throws Exception
	{
		repository = createRepository();
		conn = repository.getConnection();
		conn.add(RDF.NIL, RDF.TYPE, RDF.LIST);
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
}
