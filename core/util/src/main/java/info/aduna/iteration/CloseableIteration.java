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

/**
 * An {@link Iteration} that can be closed to free resources that it is holding.
 * CloseableIterations automatically free their resources when exhausted. If not
 * read until exhaustion or if you want to make sure the iteration is properly
 * closed, any code using the iterator should be placed in a try-with-resources
 * block, closing the iteration automatically, e.g.:
 * 
 * <pre>
 * 
 * try (CloseableIteration&lt;Object, Exception&gt; iter = ...) {
 *    // read objects from the iterator
 * }
 * catch(Exception e) {
 *   // process the exception that can be thrown while processing.
 * }
 * </pre>
 */
public interface CloseableIteration<E, X extends Exception> extends Iteration<E, X>, AutoCloseable {

	/**
	 * Closes this iteration, freeing any resources that it is holding. If the
	 * iteration has already been closed then invoking this method has no effect.
	 */
	@Override
	public void close()
		throws X;

}
