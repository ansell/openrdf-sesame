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
package org.openrdf.repository.sail.nativerdf;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import info.aduna.io.FileUtil;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.Iterations;

import org.openrdf.IsolationLevel;
import org.openrdf.IsolationLevels;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;

/**
 *
 * @author James Leigh
 */
@RunWith(Parameterized.class)
public class TestNativeStoreMemoryOverflow {

	@Parameters(name="{0}")
	public static final IsolationLevel[] parameters() {
		return IsolationLevels.values();
	}

	private File dataDir;

	private Repository testRepository;

	private RepositoryConnection testCon;

	private RepositoryConnection testCon2;

	private IsolationLevel level;

	public TestNativeStoreMemoryOverflow(IsolationLevel level) {
		this.level = level;
	}

	@Before
	public void setUp()
		throws Exception
	{
		testRepository = createRepository();
		testRepository.initialize();

		testCon = testRepository.getConnection();
		testCon.setIsolationLevel(level);
		testCon.clear();
		testCon.clearNamespaces();

		testCon2 = testRepository.getConnection();
		testCon2.setIsolationLevel(level);
	}

	private Repository createRepository()
		throws IOException
	{
		dataDir = FileUtil.createTempDir("nativestore");
		return new SailRepository(new NativeStore(dataDir, "spoc"));
	}

	@After
	public void tearDown()
		throws Exception
	{
		try {
			testCon2.close();
			testCon.close();
			testRepository.shutDown();
		}
		finally {
			FileUtil.deleteDir(dataDir);
		}
	}

	@Test
	public void test()
		throws Exception
	{
		int size = 10000; // this should really be bigger
		// load a lot of triples in two different contexts
		testCon.begin();
		final ValueFactory vf = testCon.getValueFactory();
		URI context1 = vf.createURI("http://my.context.1");
		URI context2 = vf.createURI("http://my.context.2");
		final URI predicate = vf.createURI("http://my.predicate");
		final URI object = vf.createURI("http://my.object");

		testCon.add(new DynamicIteration(size, predicate, object, vf), context1);
		testCon.add(new DynamicIteration(size, predicate, object, vf), context2);

		assertEquals(size, Iterations.asList(testCon.getStatements(null, null, null, false, context1)).size());
		assertEquals(size, Iterations.asList(testCon.getStatements(null, null, null, false, context2)).size());
		testCon.commit();

		assertEquals(size, Iterations.asList(testCon.getStatements(null, null, null, false, context1)).size());
		assertEquals(size, Iterations.asList(testCon.getStatements(null, null, null, false, context2)).size());

		testCon.close();
	}

	private static final class DynamicIteration implements Iteration<Statement, RuntimeException> {
	
		private final int size;

		private final URI predicate;
	
		private final URI object;
	
		private final ValueFactory vf;
	
		private int i;
	
		private DynamicIteration(int size, URI predicate, URI object, ValueFactory vf) {
			this.size = size;
			this.predicate = predicate;
			this.object = object;
			this.vf = vf;
		}
	
		@Override
		public boolean hasNext()
			throws RuntimeException
		{
			return i < size;
		}
	
		@Override
		public Statement next()
			throws RuntimeException
		{
			return vf.createStatement(vf.createURI("http://my.subject" + i++), predicate, object);
		}
	
		@Override
		public void remove()
			throws RuntimeException
		{
			throw new UnsupportedOperationException();
		}
	}
}
