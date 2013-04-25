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
 * An Iteration that returns the intersection of the results of two Iterations.
 * Optionally, the Iteration can be configured to filter duplicates from the
 * returned elements.
 * <p>
 * Note that duplicates can also be filtered by wrapping this Iteration in a
 * {@link DistinctIteration}, but that has a bit more overhead as it adds a
 * second hash table lookup.
 */
public class IntersectIteration<E, X extends Exception> extends FilterIteration<E, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	protected final Iteration<? extends E, ? extends X> arg2;

	private final boolean distinct;

	private boolean initialized;

	private Set<E> includeSet;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new IntersectIteration that returns the intersection of the
	 * results of two Iterations. By default, duplicates are <em>not</em>
	 * filtered from the results.
	 * 
	 * @param arg1
	 *        An Iteration containing the first set of elements.
	 * @param arg2
	 *        An Iteration containing the second set of elements.
	 */
	public IntersectIteration(Iteration<? extends E, ? extends X> arg1,
			Iteration<? extends E, ? extends X> arg2)
	{
		this(arg1, arg2, false);
	}

	/**
	 * Creates a new IntersectIteration that returns the intersection of the
	 * results of two Iterations.
	 * 
	 * @param arg1
	 *        An Iteration containing the first set of elements.
	 * @param arg2
	 *        An Iteration containing the second set of elements.
	 * @param distinct
	 *        Flag indicating whether duplicate elements should be filtered from
	 *        the result.
	 */
	public IntersectIteration(Iteration<? extends E, ? extends X> arg1,
			Iteration<? extends E, ? extends X> arg2, boolean distinct)
	{
		super(arg1);

		assert arg2 != null;

		this.arg2 = arg2;
		this.distinct = distinct;
		this.initialized = false;
	}

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Returns <tt>true</tt> if the object is in the set of elements of the
	 * second argument.
	 */
	protected boolean accept(E object)
		throws X
	{
		if (!initialized) {
			// Build set of elements-to-include from second argument
			includeSet = addSecondSet(arg2, makeSet());
			initialized = true;
		}

		if (inIncludeSet(object)) {
			// Element is part of the result

			if (distinct) {
				// Prevent duplicates from being returned by
				// removing the element from the include set
				removeFromIncludeSet(object);
			}

			return true;
		}

		return false;
	}

	public Set<E> addSecondSet(Iteration<? extends E, ? extends X> arg2, Set<E> set)
		throws X
	{
		return Iterations.addAll(arg2, makeSet());
	}

	protected boolean removeFromIncludeSet(E object) {
		return includeSet.remove(object);
	}

	protected boolean inIncludeSet(E object) {
		return includeSet.contains(object);
	}

	protected Set<E> makeSet() {
		return new HashSet<E>();
	}

	@Override
	protected void handleClose()
		throws X
	{
		super.handleClose();
		Iterations.closeCloseable(arg2);
	}
	
	protected long clearIncludeSet() {
		long size = includeSet.size();
		includeSet.clear();
		return size;
	}

}
