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
	private final Set<E> excludeSet;

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

		excludeSet = makeSet();
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Returns <tt>true</tt> if the specified object hasn't been seen before.
	 */
	@Override
	protected boolean accept(E object)
		throws X
	{
		if (inExcludeSet(object)) {
			// object has already been returned
			return false;
		}
		else {
			add(object);
			return true;
		}
	}

	/**
	 * @param object
	 * @return true if the object is in the excludeSet
	 */
	private boolean inExcludeSet(E object) {
		return excludeSet.contains(object);
	}

	/**
	 * @param object
	 *        to put into the set
	 */
	protected boolean add(E object)
		throws X
	{
		return excludeSet.add(object);
	}

	protected Set<E> makeSet() {
		return new HashSet<E>();
	}

}
