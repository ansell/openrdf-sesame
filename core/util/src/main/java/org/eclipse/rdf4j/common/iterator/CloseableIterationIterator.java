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

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import org.eclipse.rdf4j.common.iteration.Iteration;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.util.iterators.Iterators;


/**
 * Wraps an Iteration as an Iterator.
 * If the Iteration is a CloseableIteration then this.close() will close it
 * and it will also be automatically closed when this Iterator is exhausted. 
 * @author Mark
 */
public class CloseableIterationIterator<E> implements Iterator<E>, Closeable {
	private final Iteration<? extends E, ? extends RuntimeException> iteration;

	public CloseableIterationIterator(Iteration<? extends E, ? extends RuntimeException> iteration) {
		this.iteration = iteration;
	}

	@Override
	public boolean hasNext() {
		boolean hasMore = iteration.hasNext();
		if(!hasMore) {
			Iterators.closeSilently(this);
		}
		return hasMore;
	}

	@Override
	public E next() {
		return iteration.next();
	}

	@Override
	public void remove() {
		iteration.remove();
	}

	@Override
	public void close()
		throws IOException
	{
		Iterations.closeCloseable(iteration);
	}
}
