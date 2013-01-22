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
