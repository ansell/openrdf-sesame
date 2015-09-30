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
package org.eclipse.rdf4j.repository.sail.nativerdf;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.common.io.FileUtil;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnectionTest;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.sail.nativerdf.LimitedSizeNativeStore;
import org.eclipse.rdf4j.sail.nativerdf.LimitedSizeNativeStoreConnection;
import org.junit.Test;

public class LimitedSizeNativeStoreConnectionTest extends RepositoryConnectionTest {

	private File dataDir;

	public LimitedSizeNativeStoreConnectionTest(IsolationLevel level) {
		super(level);
	}

	@Override
	protected Repository createRepository()
		throws IOException
	{
		dataDir = FileUtil.createTempDir("nativestore");
		return new SailRepository(new LimitedSizeNativeStore(dataDir, "spoc"));
	}

	@Override
	public void tearDown()
		throws Exception
	{
		try {
			super.tearDown();
		}
		finally {
			FileUtil.deleteDir(dataDir);
		}
	}

	@Test
	public void testSES715()
		throws Exception
	{
		// load 1000 triples in two different contexts
		testCon.begin();
		ValueFactory vf = testCon.getValueFactory();
		IRI context1 = vf.createIRI("http://my.context.1");
		IRI context2 = vf.createIRI("http://my.context.2");
		IRI predicate = vf.createIRI("http://my.predicate");
		IRI object = vf.createIRI("http://my.object");

		for (int j = 0; j < 1000; j++) {
			testCon.add(vf.createIRI("http://my.subject" + j), predicate, object, context1);
			testCon.add(vf.createIRI("http://my.subject" + j), predicate, object, context2);
		}
		assertEquals(1000, Iterations.asList(testCon.getStatements(null, null, null, false, context1)).size());
		assertEquals(1000, Iterations.asList(testCon.getStatements(null, null, null, false, context2)).size());

		// remove all triples from context 1
		testCon.clear(context1);
		assertEquals(0, Iterations.asList(testCon.getStatements(null, null, null, false, context1)).size());
		assertEquals(1000, Iterations.asList(testCon.getStatements(null, null, null, false, context2)).size());
		testCon.commit();

		// check context content using fresh connection
		assertEquals(0, Iterations.asList(testCon2.getStatements(null, null, null, false, context1)).size());
		assertEquals(1000, Iterations.asList(testCon2.getStatements(null, null, null, false, context2)).size());

		testCon2.close();
	}

	@Test
	public void testLimit()
		throws Exception
	{
		((LimitedSizeNativeStoreConnection)((SailRepositoryConnection)testCon).getSailConnection()).setMaxCollectionsSize(2);
		testCon.begin();
		ValueFactory vf = testCon.getValueFactory();
		IRI context1 = vf.createIRI("http://my.context.1");
		IRI predicate = vf.createIRI("http://my.predicate");
		IRI object = vf.createIRI("http://my.object");

		for (int j = 0; j < 100; j++) {
			testCon.add(vf.createIRI("http://my.subject" + j), predicate, object, context1);
		}
		testCon.commit();
		String queryString = "SELECT DISTINCT ?s WHERE {?s ?p ?o}";
		TupleQuery q = testCon.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		QueryEvaluationException shouldThrow = runQuery(q);
		assertNotNull(shouldThrow);

		// There is just one object therefore we should not throw a new exception
		queryString = "SELECT DISTINCT ?o WHERE {?s ?p ?o}";
		q = testCon.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		shouldThrow = runQuery(q);
		assertNull(shouldThrow);
	}
	
	@Test
	public void testOrderAndLimit()
			throws Exception
		{
			((LimitedSizeNativeStoreConnection)((SailRepositoryConnection)testCon).getSailConnection()).setMaxCollectionsSize(2);
			testCon.begin();
			ValueFactory vf = testCon.getValueFactory();
			IRI context1 = vf.createIRI("http://my.context.1");
			IRI predicate = vf.createIRI("http://my.predicate");
			IRI object = vf.createIRI("http://my.object");

			for (int j = 0; j < 100; j++) {
				testCon.add(vf.createIRI("http://my.subject" + j), predicate, object, context1);
			}
			testCon.commit();
			String queryString = "SELECT DISTINCT ?s WHERE {?s ?p ?o} ORDER BY ?s";
			TupleQuery q = testCon.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			QueryEvaluationException shouldThrow = runQuery(q);
			assertNotNull(shouldThrow);
			
			queryString = "SELECT DISTINCT ?s WHERE {?s ?p ?o} ORDER BY ?s LIMIT 2";
			q = testCon.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			shouldThrow = runQuery(q);
			assertNull(shouldThrow);
		}

	protected QueryEvaluationException runQuery(TupleQuery q) {
		QueryEvaluationException shouldThrow = null;
		try {
			TupleQueryResult r = q.evaluate();
			assertTrue(r.hasNext());
			while (r.hasNext()) {
				assertNotNull(r.next());
			}
		}
		catch (QueryEvaluationException e) {
			shouldThrow = e;
		}
		return shouldThrow;
	}
}
