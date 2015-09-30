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

import java.util.Arrays;
import java.util.Iterator;

/**
 * An Iteration that returns the bag union of the results of a number of
 * Iterations. 'Bag union' means that the UnionIteration does not filter
 * duplicate objects.
 */
public class UnionIteration<E, X extends Exception> extends LookAheadIteration<E, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final Iterator<? extends Iteration<? extends E, X>> argIter;

	private volatile Iteration<? extends E, X> currentIter;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new UnionIteration that returns the bag union of the results of
	 * a number of Iterations.
	 * 
	 * @param args
	 *        The Iterations containing the elements to iterate over.
	 */
	public UnionIteration(Iteration<? extends E, X>... args) {
		this(Arrays.asList(args));
	}

	/**
	 * Creates a new UnionIteration that returns the bag union of the results of
	 * a number of Iterations.
	 * 
	 * @param args
	 *        The Iterations containing the elements to iterate over.
	 */
	public UnionIteration(Iterable<? extends Iteration<? extends E, X>> args) {
		argIter = args.iterator();

		// Initialize with empty iteration so that var is never null
		currentIter = new EmptyIteration<E, X>();
	}

	/*--------------*
	 * Constructors *
	 *--------------*/

	protected E getNextElement()
		throws X
	{
		if (currentIter.hasNext()) {
			return currentIter.next();
		}

		// Current Iteration exhausted, continue with the next one
		Iterations.closeCloseable(currentIter);

		synchronized (this) {
			if (argIter.hasNext()) {
				currentIter = argIter.next();
			}
			else {
				// All elements have been returned
				return null;
			}
		}

		return getNextElement();
	}

	@Override
	protected void handleClose()
		throws X
	{
		// Close this iteration, this will prevent lookAhead() from calling
		// getNextElement() again
		super.handleClose();

		synchronized (this) {
			while (argIter.hasNext()) {
				Iterations.closeCloseable(argIter.next());
			}
		}

		Iterations.closeCloseable(currentIter);
	}
}