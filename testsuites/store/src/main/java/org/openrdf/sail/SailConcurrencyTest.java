/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail;

import java.util.Random;

import junit.framework.TestCase;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Resource;
import org.openrdf.model.impl.URIImpl;

/**
 * Tests concurrent read and write access to a Sail implementation.
 * 
 * @author Arjohn Kampman
 */
public abstract class SailConcurrencyTest extends TestCase {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final int MAX_STATEMENT_IDX = 1000;

	private static final long MAX_TEST_TIME = 30 * 1000;

	/*-----------*
	 * Variables *
	 *-----------*/

	private Sail store;

	private boolean m_failed;

	private boolean continueRunning;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SailConcurrencyTest(String name) {
		super(name);
	}

	/*---------*
	 * Methods *
	 *---------*/

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

	public void testGetContextIDs()
		throws Exception
	{
		// Create one thread which writes statements to the repository, on a
		// number of named graphs.
		final Random insertRandomizer = new Random(12345L);
		final Random removeRandomizer = new Random(System.currentTimeMillis());

		Runnable writer = new Runnable() {

			public void run() {
				try {
					SailConnection connection = store.getConnection();
					try {
						while (continueRunning) {
							connection.begin();
							for (int i = 0; i < 10; i++) {
								insertTestStatement(connection, insertRandomizer.nextInt() % MAX_STATEMENT_IDX);
								removeTestStatement(connection, removeRandomizer.nextInt() % MAX_STATEMENT_IDX);
							}
							// System.out.print("*");
							connection.commit();
						}
					}
					finally {
						connection.close();
					}
				}
				catch (Throwable t) {
					continueRunning = false;
					fail("Writer failed", t);
				}
			}
		};

		// Create another which constantly calls getContextIDs() on the
		// connection.
		Runnable reader = new Runnable() {

			public void run() {
				try {
					SailConnection connection = store.getConnection();
					try {
						while (continueRunning) {
							CloseableIteration<? extends Resource, SailException> contextIter = connection.getContextIDs();
							try {
								int contextCount = 0;
								while (contextIter.hasNext()) {
									Resource context = contextIter.next();
									assertNotNull(context);
									contextCount++;
								}
								// System.out.println("Found " + contextCount + "
								// contexts");
							}
							finally {
								contextIter.close();
							}
						}
					}
					finally {
						connection.close();
					}
				}
				catch (Throwable t) {
					continueRunning = false;
					fail("Reader failed", t);
				}
			}
		};

		Thread readerThread1 = new Thread(reader);
		Thread readerThread2 = new Thread(reader);
		Thread writerThread1 = new Thread(writer);
		Thread writerThread2 = new Thread(writer);

		System.out.println("Running concurrency test...");

		continueRunning = true;
		readerThread1.start();
		readerThread2.start();
		writerThread1.start();
		writerThread2.start();

		readerThread1.join(MAX_TEST_TIME);

		continueRunning = false;

		readerThread1.join(1000);
		readerThread2.join(1000);
		writerThread1.join(1000);
		writerThread2.join(1000);

		if (hasFailed()) {
			fail("Test Failed");
		}
		else {
			System.out.println("Test succeeded");
		}
	}

	protected synchronized void fail(String message, Throwable t) {
		System.err.println(message);
		t.printStackTrace();
		m_failed = true;
	}

	protected synchronized boolean hasFailed() {
		return m_failed;
	}

	protected void insertTestStatement(SailConnection connection, int i)
		throws SailException
	{
		// System.out.print("+");
		connection.addStatement(new URIImpl("http://test#s" + i), new URIImpl("http://test#p" + i),
				new URIImpl("http://test#o" + i), new URIImpl("http://test#context_" + i));
	}

	protected void removeTestStatement(SailConnection connection, int i)
		throws SailException
	{
		// System.out.print("-");
		connection.removeStatements(new URIImpl("http://test#s" + i), new URIImpl("http://test#p" + i),
				new URIImpl("http://test#o" + i), new URIImpl("http://test#context_" + i));
	}
}
