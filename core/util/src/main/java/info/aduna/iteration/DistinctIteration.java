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
