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
package org.openrdf.sail;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;

/**
 * Tests thread interrupts on a Sail implementation.
 * 
 * @author Arjohn Kampman
 */
public abstract class SailInterruptTest {

	private Sail store;

	@Before
	public void setUp()
		throws Exception
	{
		store = createSail();
		store.initialize();
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
//		System.out.println("Preparing data set for query interruption test");
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
//						System.out.println("query sail...");
						iterateStatements();
					}
					catch (Throwable t) {
						// t.printStackTrace();
					}
				}
			}
		};

//		System.out.println("Starting query thread...");
		Thread queryThread = new Thread(queryJob);
		queryThread.start();

		queryThread.join(50);

//		System.out.println("Interrupting query thread...");
		queryThread.interrupt();

//		System.out.println("Waiting for query thread to finish...");
		queryThread.join();

//		System.out.println("Verifying that the sail can still be queried...");
		iterateStatements();

//		System.out.println("Done");
	}

	private void insertTestStatement(SailConnection connection, int seed)
		throws SailException
	{
		URI subj = new URIImpl("http://test#s" + seed % 293);
		URI pred = new URIImpl("http://test#p" + seed % 29);
		Literal obj = new LiteralImpl(Integer.toString(seed % 2903));
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
