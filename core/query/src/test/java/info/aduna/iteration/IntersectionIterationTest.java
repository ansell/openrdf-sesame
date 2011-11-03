/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.iteration;

import java.util.ArrayList;
import java.util.List;

public class IntersectionIterationTest extends CloseableIterationTest {

	@Override
	protected CloseableIteration<String, Exception> createTestIteration()
	{
		return new IntersectIteration<String, Exception>(createStringList1Iteration(),
				createStringList2Iteration());
	}

	@Override
	protected int getTestIterationSize()
	{
		List<String> intersection = new ArrayList<String>(stringList1);
		intersection.retainAll(stringList2);
		return intersection.size();
	}
}
