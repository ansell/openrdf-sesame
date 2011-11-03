/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.iteration;

import java.util.HashSet;
import java.util.Set;

public class DistinctIntersectionIterationTest extends CloseableIterationTest {

	@Override
	protected CloseableIteration<String, Exception> createTestIteration()
	{
		return new IntersectIteration<String, Exception>(createStringList1Iteration(),
				createStringList2Iteration(), true);
	}

	@Override
	protected int getTestIterationSize()
	{
		Set<String> intersection = new HashSet<String>(stringList1);
		intersection.retainAll(stringList2);
		return intersection.size();
	}
}
