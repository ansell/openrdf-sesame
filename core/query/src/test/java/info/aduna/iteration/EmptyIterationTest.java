/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.iteration;

public class EmptyIterationTest extends CloseableIterationTest {

	protected static EmptyIteration<String, Exception> createEmptyIteration() {
		return new EmptyIteration<String, Exception>();
	}

	@Override
	protected CloseableIteration<String, Exception> createTestIteration()
	{
		return createEmptyIteration();
	}

	@Override
	protected int getTestIterationSize()
	{
		return 0;
	}
}
