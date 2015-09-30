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
