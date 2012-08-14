/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.iteration;

public class ExceptionConvertingIterationTest extends CloseableIterationTest {

	@Override
	protected CloseableIteration<String, Exception> createTestIteration()
	{
		return new ExceptionConvertingIteration<String, Exception>(createStringList1Iteration()) {

			@Override
			protected Exception convert(Exception e)
			{
				return e;
			}

		};
	}

	@Override
	protected int getTestIterationSize()
	{
		return stringList1.size();
	}
}
