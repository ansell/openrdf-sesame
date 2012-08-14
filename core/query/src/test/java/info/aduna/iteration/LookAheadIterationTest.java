/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.iteration;

import java.util.Iterator;

public class LookAheadIterationTest extends CloseableIterationTest {

	@Override
	protected CloseableIteration<String, Exception> createTestIteration()
	{
		final Iterator<String> iter = stringList1.iterator();

		return new LookAheadIteration<String, Exception>() {

			@Override
			protected String getNextElement()
				throws Exception
			{
				if (iter.hasNext()) {
					return iter.next();
				}
				else {
					return null;
				}
			}

		};
	}

	@Override
	protected int getTestIterationSize()
	{
		return stringList1.size();
	}
}
