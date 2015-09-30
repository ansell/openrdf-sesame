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
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnectionTest;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;
import org.junit.Test;

public class NativeStoreConnectionTest extends RepositoryConnectionTest {

	private File dataDir;

	public NativeStoreConnectionTest(IsolationLevel level) {
		super(level);
	}

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
}
