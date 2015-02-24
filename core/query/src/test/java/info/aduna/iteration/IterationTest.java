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

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public abstract class IterationTest {

	protected static final List<String> stringList1 = Arrays.asList("1", "2", "3", "4", "5", "1", "2", "3",
			"4", "5");

	protected static final List<String> stringList2 = Arrays.asList("4", "5", "6", "7", "8");

	protected static CloseableIteration<String, Exception> createStringList1Iteration() {
		return new CloseableIteratorIteration<String, Exception>(stringList1.iterator());
	}

	protected static CloseableIteration<String, Exception> createStringList2Iteration() {
		return new CloseableIteratorIteration<String, Exception>(stringList2.iterator());
	}

	protected abstract Iteration<String, Exception> createTestIteration()
		throws Exception;

	protected abstract int getTestIterationSize();

	@Test
	public void testFullIteration()
		throws Exception
	{
		Iteration<String, Exception> iter = createTestIteration();
		int count = 0;

		while (iter.hasNext()) {
			iter.next();
			count++;
		}

		assertEquals("test iteration contains incorrect number of elements", getTestIterationSize(), count);
	}
}
