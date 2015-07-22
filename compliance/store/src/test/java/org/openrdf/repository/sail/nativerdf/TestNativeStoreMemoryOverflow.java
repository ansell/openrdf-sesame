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
