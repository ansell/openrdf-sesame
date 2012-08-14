/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.iteration;

import java.util.Collections;

public class FilterIterationTest extends CloseableIterationTest {

	@Override
	protected CloseableIteration<String, Exception> createTestIteration()
	{
		return new FilterIteration<String, Exception>(createStringList1Iteration()) {

			@Override
			protected boolean accept(String object)
				throws Exception
			{
				return "3".equals(object);
			}

		};
	}

	@Override
	protected int getTestIterationSize()
	{
		return Collections.frequency(stringList1, "3");
	}
}
