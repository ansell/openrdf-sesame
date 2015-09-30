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
package org.eclipse.rdf4j.sail;

import java.util.Random;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests thread interrupts on a Sail implementation.
 * 
 * @author Arjohn Kampman
 */
public abstract class SailInterruptTest {

	private Sail store;

	private ValueFactory vf;

	@Before
	public void setUp()
		throws Exception
	{
		store = createSail();
		store.initialize();
		vf = store.getValueFactory();
	}

	protected abstract Sail createSail()
		throws SailException;

	@After
	public void tearDown()
		throws Exception
	{
		store.shutDown();
	}

	@Test
	public void testQueryInterrupt()
		throws Exception
	{
		// System.out.println("Preparing data set for query interruption test");
		final Random r = new Random(12345);
		SailConnection con = store.getConnection();
		try {
			con.begin();
			for (int i = 0; i < 1000; i++) {
				insertTestStatement(con, r.nextInt());
			}
			con.commit();
		}
		finally {
			con.close();
		}

		Runnable queryJob = new Runnable() {

			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					try {
						// System.out.println("query sail...");
						iterateStatements();
					}
					catch (Throwable t) {
						// t.printStackTrace();
					}
				}
			}
		};

		// System.out.println("Starting query thread...");
		Thread queryThread = new Thread(queryJob);
		queryThread.start();

		queryThread.join(50);

		// System.out.println("Interrupting query thread...");
		queryThread.interrupt();

		// System.out.println("Waiting for query thread to finish...");
		queryThread.join();

		// System.out.println("Verifying that the sail can still be queried...");
		iterateStatements();

		// System.out.println("Done");
	}

	private void insertTestStatement(SailConnection connection, int seed)
		throws SailException
	{
		IRI subj = vf.createIRI("http://test#s" + seed % 293);
		IRI pred = vf.createIRI("http://test#p" + seed % 29);
		Literal obj = vf.createLiteral(Integer.toString(seed % 2903));
		connection.addStatement(subj, pred, obj);
	}

	private void iterateStatements()
		throws SailException
	{
		SailConnection con = store.getConnection();
		try {
			CloseableIteration<?, SailException> iter = con.getStatements(null, null, null, true);
			try {
				while (iter.hasNext()) {
					iter.next();
				}
			}
			finally {
				iter.close();
			}
		}
		finally {
			con.close();
		}
	}
}
