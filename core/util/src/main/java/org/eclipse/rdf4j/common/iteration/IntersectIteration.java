/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/

package org.eclipse.rdf4j.common.iteration;

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
