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
package org.openrdf.sail.memory;

import java.io.File;

import junit.framework.TestCase;

import info.aduna.io.FileUtil;
import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

public class StoreSerializationTest extends TestCase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private File dataDir;

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected void setUp()
		throws Exception
	{
		super.setUp();
		dataDir = FileUtil.createTempDir("memorystore");
	}

	@Override
	protected void tearDown()
		throws Exception
	{
		super.tearDown();
		FileUtil.deleteDir(dataDir);
	}

	public void testShortLiterals()
		throws Exception
	{
		MemoryStore store = new MemoryStore(dataDir);
		store.initialize();

		ValueFactory factory = store.getValueFactory();
		IRI foo = factory.createIRI("http://www.foo.example/foo");

		StringBuilder sb = new StringBuilder(4);
		for (int i = 0; i < 4; i++) {
			sb.append('a');
		}

		Literal longLiteral = factory.createLiteral(sb.toString());

		SailConnection con = store.getConnection();
		con.begin();
		con.addStatement(foo, RDF.VALUE, longLiteral);
		con.commit();

		con.close();
		store.shutDown();

		store = new MemoryStore(dataDir);
		store.initialize();

		con = store.getConnection();

		CloseableIteration<? extends Statement, SailException> iter = con.getStatements(foo, RDF.VALUE, null,
				false);
		assertTrue(iter.hasNext());
		iter.next();
		iter.close();

		con.close();
		store.shutDown();		
	}

	public void testSerialization()
		throws Exception
	{
		MemoryStore store = new MemoryStore(dataDir);
		store.initialize();

		ValueFactory factory = store.getValueFactory();
		IRI foo = factory.createIRI("http://www.foo.example/foo");
		IRI bar = factory.createIRI("http://www.foo.example/bar");

		SailConnection con = store.getConnection();
		con.begin();
		con.addStatement(foo, RDF.TYPE, bar);
		con.commit();

		ParsedTupleQuery query = QueryParserUtil.parseTupleQuery(QueryLanguage.SERQL,
				"SELECT X, P, Y FROM {X} P {Y}", null);
		TupleExpr tupleExpr = query.getTupleExpr();

		CloseableIteration<? extends BindingSet, QueryEvaluationException> iter = con.evaluate(tupleExpr, null,
				EmptyBindingSet.getInstance(), false);

		BindingSet bindingSet = iter.next();

		assertEquals(bindingSet.getValue("X"), foo);
		assertEquals(bindingSet.getValue("P"), RDF.TYPE);
		assertEquals(bindingSet.getValue("Y"), bar);
		iter.close();
		con.close();

		store.shutDown();

		store = new MemoryStore(dataDir);
		store.initialize();

		factory = store.getValueFactory();
		foo = factory.createIRI("http://www.foo.example/foo");
		bar = factory.createIRI("http://www.foo.example/bar");

		con = store.getConnection();

		iter = con.evaluate(tupleExpr, null, EmptyBindingSet.getInstance(), false);

		bindingSet = iter.next();

		assertEquals(bindingSet.getValue("X"), foo);
		assertEquals(bindingSet.getValue("P"), RDF.TYPE);
		assertEquals(bindingSet.getValue("Y"), bar);

		iter.close();
		con.begin();
		con.addStatement(bar, RDF.TYPE, foo);
		con.commit();
		con.close();
		
		store.shutDown();
	}

	public void testLongLiterals()
		throws Exception
	{
		MemoryStore store = new MemoryStore(dataDir);
		store.initialize();

		ValueFactory factory = store.getValueFactory();
		IRI foo = factory.createIRI("http://www.foo.example/foo");

		StringBuilder sb = new StringBuilder(66000);
		for (int i = 0; i < 66000; i++) {
			sb.append('a');
		}

		Literal longLiteral = factory.createLiteral(sb.toString());

		SailConnection con = store.getConnection();
		con.begin();
		con.addStatement(foo, RDF.VALUE, longLiteral);
		con.commit();

		con.close();
		store.shutDown();

		store = new MemoryStore(dataDir);
		store.initialize();

		con = store.getConnection();

		CloseableIteration<? extends Statement, SailException> iter = con.getStatements(foo, RDF.VALUE, null,
				false);
		assertTrue(iter.hasNext());
		iter.next();
		iter.close();

		con.close();
		store.shutDown();		
	}
}
