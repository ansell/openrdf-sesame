/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.iteration;


public class SingletonIterationTest extends CloseableIterationTest {

	@Override
	protected CloseableIteration<String, Exception> createTestIteration()
	{
		return new SingletonIteration<String, Exception>("3");
	}

	@Override
	protected int getTestIterationSize()
	{
		return 1;
	}
}
