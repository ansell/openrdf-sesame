/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail;

import java.util.Random;

import junit.framework.TestCase;

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
public abstract class SailInterruptTest extends TestCase {

	private Sail store;

	public SailInterruptTest(String name) {
		super(name);
	}

	@Override
	protected void setUp()
		throws Exception
	{
		super.setUp();
		store = createSail();
		store.initialize();
	}

	protected abstract Sail createSail()
		throws SailException;

	@Override
	protected void tearDown()
		throws Exception
	{
		try {
			store.shutDown();
		}
		finally {
			super.tearDown();
		}
	}

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
