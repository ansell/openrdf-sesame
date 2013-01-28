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

import java.util.Arrays;
import java.util.List;

public class ConvertingIterationTest extends CloseableIterationTest {

	private static final List<Integer> intList = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

	protected static CloseableIteration<String, Exception> createConvertingIteration() {
		Iteration<Integer, Exception> intIteration = new CloseableIteratorIteration<Integer, Exception>(intList.iterator());
		return new ConvertingIteration<Integer, String, Exception>(intIteration) {
			protected String convert(Integer integer) {
				return integer.toString();
			}
		};
	}

	@Override
	protected CloseableIteration<String, Exception> createTestIteration()
	{
		return createConvertingIteration();
	}

	@Override
	protected int getTestIterationSize()
	{
		return 10;
	}
}
