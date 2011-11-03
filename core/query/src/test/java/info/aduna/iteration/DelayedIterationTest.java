/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.iteration;

public class DelayedIterationTest extends CloseableIterationTest {

	@Override
	protected CloseableIteration<String, Exception> createTestIteration()
	{
		return new DelayedIteration<String, Exception>() {

			@Override
			protected Iteration<? extends String, Exception> createIteration()
			{
				return createStringList1Iteration();
			}
		};
	}

	@Override
	protected int getTestIterationSize()
	{
		return stringList1.size();
	}
}
