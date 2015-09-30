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
package org.openrdf.repository;

import static org.junit.Assert.assertFalse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;

public abstract class CascadeValueExceptionTest {

	private static String queryStrLT = "SELECT * WHERE { ?s ?p ?o FILTER( !(\"2002\" < \"2007\"^^<http://www.w3.org/2001/XMLSchema#gYear>))}";

	private static String queryStrLE = "SELECT * WHERE { ?s ?p ?o FILTER( !(\"2002\" <= \"2007\"^^<http://www.w3.org/2001/XMLSchema#gYear>))}";

	private static String queryStrEQ = "SELECT * WHERE { ?s ?p ?o FILTER( !(\"2002\" = \"2007\"^^<http://www.w3.org/2001/XMLSchema#gYear>))}";

	private static String queryStrNE = "SELECT * WHERE { ?s ?p ?o FILTER( !(\"2002\" != \"2007\"^^<http://www.w3.org/2001/XMLSchema#gYear>))}";

	private static String queryStrGE = "SELECT * WHERE { ?s ?p ?o FILTER( !(\"2002\" >= \"2007\"^^<http://www.w3.org/2001/XMLSchema#gYear>))}";

	private static String queryStrGT = "SELECT * WHERE { ?s ?p ?o FILTER( !(\"2002\" > \"2007\"^^<http://www.w3.org/2001/XMLSchema#gYear>))}";

	private static String queryStrAltLT = "SELECT * WHERE { ?s ?p ?o FILTER( !(\"2002\"^^<http://www.w3.org/2001/XMLSchema#string> < \"2007\"^^<http://www.w3.org/2001/XMLSchema#gYear>))}";

	private static String queryStrAltLE = "SELECT * WHERE { ?s ?p ?o FILTER( !(\"2002\"^^<http://www.w3.org/2001/XMLSchema#string> <= \"2007\"^^<http://www.w3.org/2001/XMLSchema#gYear>))}";

	private static String queryStrAltEQ = "SELECT * WHERE { ?s ?p ?o FILTER( !(\"2002\"^^<http://www.w3.org/2001/XMLSchema#string> = \"2007\"^^<http://www.w3.org/2001/XMLSchema#gYear>))}";

	private static String queryStrAltNE = "SELECT * WHERE { ?s ?p ?o FILTER( !(\"2002\"^^<http://www.w3.org/2001/XMLSchema#string> != \"2007\"^^<http://www.w3.org/2001/XMLSchema#gYear>))}";

	private static String queryStrAltGE = "SELECT * WHERE { ?s ?p ?o FILTER( !(\"2002\"^^<http://www.w3.org/2001/XMLSchema#string> >= \"2007\"^^<http://www.w3.org/2001/XMLSchema#gYear>))}";

	private static String queryStrAltGT = "SELECT * WHERE { ?s ?p ?o FILTER( !(\"2002\"^^<http://www.w3.org/2001/XMLSchema#string> > \"2007\"^^<http://www.w3.org/2001/XMLSchema#gYear>))}";

	private RepositoryConnection conn;

	private Repository repository;

	@Test
	public void testValueExceptionLessThanPlain()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryStrLT);
		TupleQueryResult evaluate = query.evaluate();
		try {
			assertFalse(evaluate.hasNext());
		}
		finally {
			evaluate.close();
		}
	}

	@Test
	public void testValueExceptionLessThanOrEqualPlain()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryStrLE);
		TupleQueryResult evaluate = query.evaluate();
		try {
			assertFalse(evaluate.hasNext());
		}
		finally {
			evaluate.close();
		}
	}

	@Test
	public void testValueExceptionEqualPlain()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryStrEQ);
		TupleQueryResult evaluate = query.evaluate();
		try {
			assertFalse(evaluate.hasNext());
		}
		finally {
			evaluate.close();
		}
	}

	@Test
	public void testValueExceptionNotEqualPlain()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryStrNE);
		TupleQueryResult evaluate = query.evaluate();
		try {
			assertFalse(evaluate.hasNext());
		}
		finally {
			evaluate.close();
		}
	}

	@Test
	public void testValueExceptionGreaterThanOrEqualPlain()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryStrGE);
		TupleQueryResult evaluate = query.evaluate();
		try {
			assertFalse(evaluate.hasNext());
		}
		finally {
			evaluate.close();
		}
	}

	@Test
	public void testValueExceptionGreaterThanPlain()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryStrGT);
		TupleQueryResult evaluate = query.evaluate();
		try {
			assertFalse(evaluate.hasNext());
		}
		finally {
			evaluate.close();
		}
	}

	@Test
	public void testValueExceptionLessThanTyped()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryStrAltLT);
		TupleQueryResult evaluate = query.evaluate();
		try {
			assertFalse(evaluate.hasNext());
		}
		finally {
			evaluate.close();
		}
	}

	@Test
	public void testValueExceptionLessThanOrEqualTyped()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryStrAltLE);
		TupleQueryResult evaluate = query.evaluate();
		try {
			assertFalse(evaluate.hasNext());
		}
		finally {
			evaluate.close();
		}
	}

	@Test
	public void testValueExceptionEqualTyped()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryStrAltEQ);
		TupleQueryResult evaluate = query.evaluate();
		try {
			assertFalse(evaluate.hasNext());
		}
		finally {
			evaluate.close();
		}
	}

	@Test
	public void testValueExceptionNotEqualTyped()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryStrAltNE);
		TupleQueryResult evaluate = query.evaluate();
		try {
			assertFalse(evaluate.hasNext());
		}
		finally {
			evaluate.close();
		}
	}

	@Test
	public void testValueExceptionGreaterThanOrEqualTyped()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryStrAltGE);
		TupleQueryResult evaluate = query.evaluate();
		try {
			assertFalse(evaluate.hasNext());
		}
		finally {
			evaluate.close();
		}
	}

	@Test
	public void testValueExceptionGreaterThanTyped()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryStrAltGT);
		TupleQueryResult evaluate = query.evaluate();
		try {
			assertFalse(evaluate.hasNext());
		}
		finally {
			evaluate.close();
		}
	}

	@Before
	public void setUp()
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

	@After
	public void tearDown()
		throws Exception
	{
		conn.close();
		conn = null;

		repository.shutDown();
		repository = null;
	}
}
