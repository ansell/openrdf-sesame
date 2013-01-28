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

public class UnionIterationTest extends CloseableIterationTest {

	@Override
	protected CloseableIteration<String, Exception> createTestIteration()
	{
		return new UnionIteration<String, Exception>(createStringList1Iteration(), createStringList2Iteration());
	}

	@Override
	protected int getTestIterationSize()
	{
		return stringList1.size() + stringList2.size();
	}

	public void testArgumentsClosed()
		throws Exception
	{
		SingletonIteration<String, Exception> iter1 = new SingletonIteration<String, Exception>("1");
		SingletonIteration<String, Exception> iter2 = new SingletonIteration<String, Exception>("2");
		SingletonIteration<String, Exception> iter3 = new SingletonIteration<String, Exception>("3");
		UnionIteration<String, Exception> unionIter = new UnionIteration<String, Exception>(iter1, iter2, iter3);

		unionIter.next();
		unionIter.close();

		assertTrue("iter1 should have been closed", iter1.isClosed());
		assertTrue("iter2 should have been closed", iter2.isClosed());
		assertTrue("iter3 should have been closed", iter3.isClosed());
	}
}
