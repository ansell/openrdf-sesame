/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.iteration;

import java.util.Collections;
import java.util.List;

public class OffsetIterationTest extends CloseableIterationTest {

	protected static OffsetIteration<String, Exception> createOffsetIteration(int offset) {
		return new OffsetIteration<String, Exception>(createStringList1Iteration(), offset);
	}

	@Override
	protected CloseableIteration<String, Exception> createTestIteration()
	{
		return createOffsetIteration(5);
	}

	@Override
	protected int getTestIterationSize()
	{
		return 5;
	}

	public void testInRangeOffset()
		throws Exception
	{
		for (int offset = 0; offset < stringList1.size(); offset++) {
			Iteration<String, Exception> iter = createOffsetIteration(offset);
			List<String> resultList = Iterations.asList(iter);
			List<String> expectedList = stringList1.subList(offset, stringList1.size());
			assertEquals("test failed for offset: " + offset, expectedList, resultList);
		}
	}

	public void testOutOfRangeOffset()
		throws Exception
	{
		Iteration<String, Exception> iter = createOffsetIteration(2 * stringList1.size());
		List<String> resultList = Iterations.asList(iter);
		assertEquals(Collections.emptyList(), resultList);
	}
}
