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
 * An Iteration that skips the first <tt>offset</tt> elements from an
 * underlying Iteration.
 */
public class OffsetIteration<E, X extends Exception> extends FilterIteration<E, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The offset (0-based) of the first element to return.
	 */
	private final long offset;

	/**
	 * The number of elements that have been dropped so far.
	 */
	private long droppedResults;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new OffsetIteration.
	 * 
	 * @param iter
	 *        The underlying Iteration, must not be <tt>null</tt>.
	 * @param offset
	 *        The number of elements to skip, must be larger than or equal to 0.
	 */
	public OffsetIteration(Iteration<? extends E, X> iter, long offset) {
		super(iter);

		assert offset >= 0;

		this.offset = offset;
		this.droppedResults = 0;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Returns <tt>false</tt> for the first OFFSET objects.
	 */
	protected boolean accept(E object) {
		if (droppedResults < offset) {
			droppedResults++;
			return false;
		}
		else {
			return true;
		}
	}
}
