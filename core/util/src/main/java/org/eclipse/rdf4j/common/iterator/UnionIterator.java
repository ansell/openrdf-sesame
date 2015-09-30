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

package org.eclipse.rdf4j.common.iterator;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.rdf4j.util.iterators.Iterators;

/**
 * @author MJAHale
 */
public class UnionIterator<E> extends LookAheadIterator<E> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final Iterator<? extends Iterable<? extends E>> argIter;

	private volatile Iterator<? extends E> currentIter;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new UnionIterator that returns the bag union of the results of
	 * a number of Iterators.
	 * 
	 * @param args
	 *        The Iterators containing the elements to iterate over.
	 */
	public UnionIterator(Iterable<? extends E>... args) {
		this(Arrays.asList(args));
	}

	public UnionIterator(Iterable<? extends Iterable<? extends E>> args) {
		argIter = args.iterator();

		// Initialize with empty iteration so that var is never null
		currentIter = Collections.emptyIterator();
	}

	/*--------------*
	 * Constructors *
	 *--------------*/

	@Override
	protected E getNextElement()
	{
		if (currentIter.hasNext()) {
			return currentIter.next();
		}

		// Current Iterator exhausted, continue with the next one
		Iterators.closeSilently(currentIter);

		if (argIter.hasNext()) {
			currentIter = argIter.next().iterator();
		}
		else {
			// All elements have been returned
			return null;
		}

		return getNextElement();
	}

	@Override
	protected void handleClose()
		throws IOException
	{
		// Close this iteration, this will prevent lookAhead() from calling
		// getNextElement() again
		super.handleClose();

		Iterators.close(currentIter);
	}
}
