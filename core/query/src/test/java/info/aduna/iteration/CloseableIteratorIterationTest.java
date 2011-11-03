/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.iteration;

public class CloseableIteratorIterationTest extends CloseableIterationTest {

	@Override
	protected CloseableIteration<String, Exception> createTestIteration()
	{
		return new CloseableIteratorIteration<String, Exception>(stringList1.iterator());
	}

	@Override
	protected int getTestIterationSize()
	{
		return stringList1.size();
	}
}
