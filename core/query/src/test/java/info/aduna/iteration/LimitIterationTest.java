/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package info.aduna.iteration;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

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

	@Test
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

	@Test
	public void testOutOfRangeOffset()
		throws Exception
	{
		Iteration<String, Exception> iter = createLimitIteration(2 * stringList1.size());
		List<String> resultList = Iterations.asList(iter);
		assertEquals(stringList1, resultList);
	}
}
