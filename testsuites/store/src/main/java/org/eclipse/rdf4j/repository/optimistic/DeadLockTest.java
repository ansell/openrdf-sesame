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
package org.eclipse.rdf4j.repository.optimistic;

import static org.junit.Assert.assertNull;

import java.util.concurrent.CountDownLatch;

import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.OptimisticIsolationTest;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DeadLockTest {
	private Repository repo;
	private RepositoryConnection a;
	private RepositoryConnection b;
	private IsolationLevel level = IsolationLevels.SNAPSHOT_READ;
	private String NS = "http://rdf.example.org/";
	private URI PAINTER;
	private URI PICASSO;
	private URI REMBRANDT;

	@Before
	public void setUp() throws Exception {
		repo = OptimisticIsolationTest.getEmptyInitializedRepository(DeadLockTest.class);
		ValueFactory uf = repo.getValueFactory();
		PAINTER = uf.createURI(NS, "Painter");
		PICASSO = uf.createURI(NS, "picasso");
		REMBRANDT = uf.createURI(NS, "rembrandt");
		a = repo.getConnection();
		b = repo.getConnection();
	}

	@After
	public void tearDown() throws Exception {
		a.close();
		b.close();
		repo.shutDown();
	}

	@Test
	public void test() throws Exception {
		final CountDownLatch start = new CountDownLatch(2);
		final CountDownLatch end = new CountDownLatch(2);
		final CountDownLatch commit = new CountDownLatch(1);
		final Exception e1 = new Exception();
		new Thread(new Runnable() {
			public void run() {
				try {
					start.countDown();
					a.begin(level);
					a.add(PICASSO, RDF.TYPE, PAINTER);
					commit.await();
					a.commit();
				} catch (Exception e) {
					e1.initCause(e);
				} finally {
					end.countDown();
				}
			}
		}).start();
		final Exception e2 = new Exception();
		new Thread(new Runnable() {
			public void run() {
				try {
					start.countDown();
					b.begin(level);
					b.add(REMBRANDT, RDF.TYPE, PAINTER);
					commit.await();
					b.commit();
				} catch (Exception e) {
					e2.initCause(e);
				} finally {
					end.countDown();
				}
			}
		}).start();
		start.await();
		commit.countDown();
		Thread.sleep(500);
		end.await();
		assertNull(e1.getCause());
		assertNull(e2.getCause());
	}

}
