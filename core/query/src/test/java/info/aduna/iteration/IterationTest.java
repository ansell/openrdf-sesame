/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.iteration;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public abstract class IterationTest extends TestCase {

	protected static final List<String> stringList1 = Arrays.asList("1", "2", "3", "4", "5", "1", "2", "3",
			"4", "5");

	protected static final List<String> stringList2 = Arrays.asList("4", "5", "6", "7", "8");

	protected static CloseableIteration<String, Exception> createStringList1Iteration() {
		return new CloseableIteratorIteration<String, Exception>(stringList1.iterator());
	}

	protected static CloseableIteration<String, Exception> createStringList2Iteration() {
		return new CloseableIteratorIteration<String, Exception>(stringList2.iterator());
	}

	protected abstract Iteration<String, Exception> createTestIteration()
		throws Exception;

	protected abstract int getTestIterationSize();

	public void testFullIteration()
		throws Exception
	{
		Iteration<String, Exception> iter = createTestIteration();
		int count = 0;

		while (iter.hasNext()) {
			iter.next();
			count++;
		}

		assertEquals("test iteration contains incorrect number of elements", getTestIterationSize(), count);
	}
}
