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
