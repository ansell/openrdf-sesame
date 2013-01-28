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

/**
 * An Iteration that filters any duplicate elements from an underlying iterator.
 */
public class DistinctIteration<E, X extends Exception> extends FilterIteration<E, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The elements that have already been returned.
	 */
	private final HashSet<E> excludeSet;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new DistinctIterator.
	 * 
	 * @param iter
	 *        The underlying iterator.
	 */
	public DistinctIteration(Iteration<? extends E, ? extends X> iter) {
		super(iter);

		excludeSet = new HashSet<E>();
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Returns <tt>true</tt> if the specified object hasn't been seen before.
	 */
	protected boolean accept(E object) {
		if (excludeSet.contains(object)) {
			// object has already been returned
			return false;
		}
		else {
			excludeSet.add(object);
			return true;
		}
	}
}
