/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.iteration;

import java.util.List;

public class LimitIterationTest extends CloseableIterationTest {

	protected static LimitIteration<String, Exception> createLimitIteration(int limit) {
		return new LimitIteration<String, Exception>(createStringList1Iteration(), limit);
	}

	@Override
	protected CloseableIteration<String, Exception> createTestIteration()
	{
		return createLimitIteration(5);
	}

	@Override
	protected int getTestIterationSize()
	{
		return 5;
	}

	public void testInRangeOffset()
		throws Exception
	{
		for (int limit = 0; limit < stringList1.size(); limit++) {
			Iteration<String, Exception> iter = createLimitIteration(limit);
			List<String> resultList = Iterations.asList(iter);
			List<String> expectedList = stringList1.subList(0, limit);
			assertEquals("testInRangeOffset failed for limit: " + limit, expectedList, resultList);
		}
	}

	public void testOutOfRangeOffset()
		throws Exception
	{
		Iteration<String, Exception> iter = createLimitIteration(2 * stringList1.size());
		List<String> resultList = Iterations.asList(iter);
		assertEquals(stringList1, resultList);
	}
}
