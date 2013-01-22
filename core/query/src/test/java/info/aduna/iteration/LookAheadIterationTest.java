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

import java.util.Iterator;

public class LookAheadIterationTest extends CloseableIterationTest {

	@Override
	protected CloseableIteration<String, Exception> createTestIteration()
	{
		final Iterator<String> iter = stringList1.iterator();

		return new LookAheadIteration<String, Exception>() {

			@Override
			protected String getNextElement()
				throws Exception
			{
				if (iter.hasNext()) {
					return iter.next();
				}
				else {
					return null;
				}
			}

		};
	}

	@Override
	protected int getTestIterationSize()
	{
		return stringList1.size();
	}
}
