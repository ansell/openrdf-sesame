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

import java.util.NoSuchElementException;

/**
 * An Iteration that limits the amount of elements that it returns from an
 * underlying Iteration to a fixed amount. This class returns the first
 * <tt>limit</tt> elements from the underlying Iteration and drops the rest.
 */
public class LimitIteration<E, X extends Exception> extends IterationWrapper<E, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The amount of elements to return.
	 */
	private final long limit;

	/**
	 * The number of elements that have been returned so far.
	 */
	private long returnCount;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new LimitIteration.
	 * 
	 * @param iter
	 *        The underlying Iteration, must not be <tt>null</tt>.
	 * @param limit
	 *        The number of query answers to return, must be &gt;= 0.
	 */
	public LimitIteration(Iteration<? extends E, X> iter, long limit) {
		super(iter);

		assert iter != null;
		assert limit >= 0;

		this.limit = limit;
		this.returnCount = 0;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public boolean hasNext()
		throws X
	{
		return returnCount < limit && super.hasNext();
	}

	@Override
	public E next()
		throws X
	{
		if (returnCount >= limit) {
			throw new NoSuchElementException("limit reached");
		}

		returnCount++;
		return super.next();
	}
}
