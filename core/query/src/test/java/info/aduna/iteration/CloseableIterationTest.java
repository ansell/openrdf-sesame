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

import java.util.NoSuchElementException;

/**
 *
 */
public abstract class CloseableIterationTest extends IterationTest {

	protected abstract CloseableIteration<String, Exception> createTestIteration()
		throws Exception;

	public void testClosedIteration()
		throws Exception
	{
		for (int n = 0; n < getTestIterationSize(); n++) {
			CloseableIteration<String, Exception> iter = createTestIteration();

			// Close after n iterations
			for (int i = 0; i < n; i++) {
				iter.next();
			}

			iter.close();

			assertFalse("closed iteration should not contain any more elements", iter.hasNext());

			try {
				iter.next();
				fail("next() called on a closed iteration should throw a NoSuchElementException");
			}
			catch (NoSuchElementException e) {
				// expected exception
			}
			catch (Exception e) {
				fail("next() called on a closed iteration should throw a NoSuchElementException");
			}
		}
	}
}
