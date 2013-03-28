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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import info.aduna.io.FileUtil;
import info.aduna.iteration.Iterations;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnectionTest;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;

public class NativeStoreConnectionTest extends RepositoryConnectionTest {

	private File dataDir;

	@Override
	protected Repository createRepository()
		throws IOException
	{
		dataDir = FileUtil.createTempDir("nativestore");
		return new SailRepository(new NativeStore(dataDir, "spoc"));
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
		URI context1 = vf.createURI("http://my.context.1");
		URI context2 = vf.createURI("http://my.context.2");
		URI predicate = vf.createURI("http://my.predicate");
		URI object = vf.createURI("http://my.object");

		for (int j = 0; j < 1000; j++) {
			testCon.add(vf.createURI("http://my.subject" + j), predicate, object, context1);
			testCon.add(vf.createURI("http://my.subject" + j), predicate, object, context2);
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
}
