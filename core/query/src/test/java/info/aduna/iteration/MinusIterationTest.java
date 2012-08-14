/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.iteration;

import java.util.ArrayList;
import java.util.List;

public class MinusIterationTest extends CloseableIterationTest {

	@Override
	protected CloseableIteration<String, Exception> createTestIteration()
	{
		return new MinusIteration<String, Exception>(createStringList1Iteration(),
				createStringList2Iteration());
	}

	@Override
	protected int getTestIterationSize()
	{
		List<String> difference = new ArrayList<String>(stringList1);
		difference.removeAll(stringList2);
		return difference.size();
	}
}
