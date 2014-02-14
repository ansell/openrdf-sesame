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
package org.openrdf.model;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

/**
 * Abstract tests for the {@link ValueFactory} interface, including concurrency
 * tests, when these are enabled.
 * 
 * @author Peter Ansell
 */
public abstract class AbstractValueFactoryTest {

	/**
	 * Timeout each test after 10 seconds.
	 */
	@Rule
	public Timeout timeout = new Timeout(10000);

	private ValueFactory vf;

	@Before
	public void setUp()
		throws Exception
	{
		vf = getNewValueFactory();
	}

	@After
	public void tearDown()
		throws Exception
	{
		vf = null;
	}

	/**
	 * Implementing tests must override this method to provide an implementation
	 * of the {@link ValueFactory} interface.
	 * 
	 * @return A new instance of the {@link ValueFactory} interface.
	 */
	protected abstract ValueFactory getNewValueFactory() throws Exception;

	/**
	 * Determines whether to enable the concurrency tests for this
	 * implementation.
	 * 
	 * @return True if the implementation is thought to be threadsafe, and should
	 *         be tested using concurrent access from different threads.
	 */
	protected abstract boolean isThreadSafe();

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createURI(java.lang.String)}.
	 */
	@Test
	public final void testCreateURIString() {
		Set<URI> allValues = new HashSet<URI>();
		for (int i = 0; i < 10000; i++) {
			allValues.add(vf.createURI("http://example.org/a" + i));
		}
		assertEquals(10000, allValues.size());
		allNonNull(allValues);
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createURI(java.lang.String)}.
	 */
	@Test
	public final void testCreateURIStringConcurrent()
		throws Exception
	{
		if (!isThreadSafe()) {
			return;
		}
		final ValueFactory finalVf = vf;
		final Set<URI> allValues = Collections.newSetFromMap(new ConcurrentHashMap<URI, Boolean>());
		final AtomicInteger count = new AtomicInteger(0);
		final CountDownLatch openLatch = new CountDownLatch(1);
		final int threadCount = 37;
		final CountDownLatch closeLatch = new CountDownLatch(threadCount);
		for (int i = 0; i < threadCount; i++) {
			final int number = i;
			Runnable runner = new Runnable() {

				@Override
				public void run() {
					try {
						openLatch.await();
						for (int j = 0; j < 10000; j++) {
							final int k = j % 371;
							final String nextUri = "urn:test" + k + "#test" + j;
							final URI result = finalVf.createURI(nextUri);
							allValues.add(result);
							assertEquals(nextUri, result.stringValue());
						}
						count.incrementAndGet();
						closeLatch.countDown();
					}
					catch (InterruptedException ie) {
						ie.printStackTrace();
						fail("Failed in test: " + number);
					}
				}
			};
			new Thread(runner, "TestThread" + number).start();
		}
		// all threads are waiting on the latch.
		openLatch.countDown(); // release the latch
		// all threads are now running concurrently.
		closeLatch.await();
		assertEquals(threadCount, count.get());
		assertEquals(10000, allValues.size());
		allNonNull(allValues);
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createURI(java.lang.String, java.lang.String)}
	 * .
	 */
	@Test
	public final void testCreateURIStringString() {
		Set<URI> allValues = new HashSet<URI>();
		for (int i = 0; i < 10000; i++) {
			allValues.add(vf.createURI("http://example.org/a", "b" + i));
		}
		assertEquals(10000, allValues.size());
		allNonNull(allValues);
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createURI(java.lang.String, java.lang.String)}
	 * .
	 */
	@Test
	public final void testCreateURIStringStringConcurrentFixedBase()
		throws Exception
	{
		if (!isThreadSafe()) {
			return;
		}
		final ValueFactory finalVf = vf;
		final Set<URI> allValues = Collections.newSetFromMap(new ConcurrentHashMap<URI, Boolean>());
		final AtomicInteger count = new AtomicInteger(0);
		final CountDownLatch openLatch = new CountDownLatch(1);
		final int threadCount = 37;
		final CountDownLatch closeLatch = new CountDownLatch(threadCount);
		for (int i = 0; i < threadCount; i++) {
			final int number = i;
			Runnable runner = new Runnable() {

				@Override
				public void run() {
					try {
						openLatch.await();
						for (int j = 0; j < 10000; j++) {
							final int k = j % 371;
							final String nextUri = "urn:test#" + k + "test" + j;
							final URI result = finalVf.createURI("urn:test#", k + "test" + j);
							allValues.add(result);
							assertEquals(nextUri, result.stringValue());
						}
						count.incrementAndGet();
						closeLatch.countDown();
					}
					catch (InterruptedException ie) {
						ie.printStackTrace();
						fail("Failed in test: " + number);
					}
				}
			};
			new Thread(runner, "TestThread" + number).start();
		}
		// all threads are waiting on the latch.
		openLatch.countDown(); // release the latch
		// all threads are now running concurrently.
		closeLatch.await();
		assertEquals(threadCount, count.get());
		assertEquals(10000, allValues.size());
		allNonNull(allValues);
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createURI(java.lang.String, java.lang.String)}
	 * .
	 */
	@Test
	public final void testCreateURIStringStringConcurrentVariableBase()
		throws Exception
	{
		if (!isThreadSafe()) {
			return;
		}
		final ValueFactory finalVf = vf;
		final Set<URI> allValues = Collections.newSetFromMap(new ConcurrentHashMap<URI, Boolean>());
		final AtomicInteger count = new AtomicInteger(0);
		final CountDownLatch openLatch = new CountDownLatch(1);
		final int threadCount = 37;
		final CountDownLatch closeLatch = new CountDownLatch(threadCount);
		for (int i = 0; i < threadCount; i++) {
			final int number = i;
			Runnable runner = new Runnable() {

				@Override
				public void run() {
					try {
						openLatch.await();
						for (int j = 0; j < 10000; j++) {
							final int k = j % 371;
							final String nextUri = "urn:test#" + k + "test" + j;
							final URI result = finalVf.createURI("urn:test#" + k, "test" + j);
							allValues.add(result);
							assertEquals(nextUri, result.stringValue());
						}
						count.incrementAndGet();
						closeLatch.countDown();
					}
					catch (InterruptedException ie) {
						ie.printStackTrace();
						fail("Failed in test: " + number);
					}
				}
			};
			new Thread(runner, "TestThread" + number).start();
		}
		// all threads are waiting on the latch.
		openLatch.countDown(); // release the latch
		// all threads are now running concurrently.
		closeLatch.await();
		assertEquals(threadCount, count.get());
		assertEquals(10000, allValues.size());
		allNonNull(allValues);
	}

	/**
	 * Test method for {@link org.openrdf.model.ValueFactory#createBNode()}.
	 */
	@Test
	public final void testCreateBNode() {
		Set<BNode> allValues = new HashSet<BNode>();
		for (int i = 0; i < 10000; i++) {
			allValues.add(vf.createBNode());
		}
		assertEquals(10000, allValues.size());
		allNonNull(allValues);
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createBNode(java.lang.String)}.
	 */
	@Test
	public final void testCreateBNodeString() {
		Set<BNode> allValues = new HashSet<BNode>();
		for (int i = 0; i < 10000; i++) {
			allValues.add(vf.createBNode(Integer.toString(i)));
		}
		assertEquals(10000, allValues.size());
		allNonNull(allValues);
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createLiteral(java.lang.String)}.
	 */
	@Ignore
	@Test
	public final void testCreateLiteralString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createLiteral(java.lang.String, java.lang.String)}
	 * .
	 */
	@Ignore
	@Test
	public final void testCreateLiteralStringString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createLiteral(java.lang.String, org.openrdf.model.URI)}
	 * .
	 */
	@Ignore
	@Test
	public final void testCreateLiteralStringURI() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createLiteral(boolean)}.
	 */
	@Ignore
	@Test
	public final void testCreateLiteralBoolean() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.ValueFactory#createLiteral(byte)}
	 * .
	 */
	@Ignore
	@Test
	public final void testCreateLiteralByte() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createLiteral(short)}.
	 */
	@Ignore
	@Test
	public final void testCreateLiteralShort() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.ValueFactory#createLiteral(int)}.
	 */
	@Ignore
	@Test
	public final void testCreateLiteralInt() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.ValueFactory#createLiteral(long)}
	 * .
	 */
	@Ignore
	@Test
	public final void testCreateLiteralLong() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createLiteral(float)}.
	 */
	@Ignore
	@Test
	public final void testCreateLiteralFloat() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createLiteral(double)}.
	 */
	@Ignore
	@Test
	public final void testCreateLiteralDouble() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createLiteral(javax.xml.datatype.XMLGregorianCalendar)}
	 * .
	 */
	@Ignore
	@Test
	public final void testCreateLiteralXMLGregorianCalendar() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createLiteral(java.util.Date)}.
	 */
	@Ignore
	@Test
	public final void testCreateLiteralDate() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createStatement(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value)}
	 * .
	 */
	@Ignore
	@Test
	public final void testCreateStatementResourceURIValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createStatement(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource)}
	 * .
	 */
	@Ignore
	@Test
	public final void testCreateStatementResourceURIValueResource() {
		fail("Not yet implemented"); // TODO
	}

	private final void allNonNull(Collection<? extends Value> testValues) {
		for (Value nextValue : testValues) {
			assertNotNull(nextValue);
		}
	}
}
