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

package org.eclipse.rdf4j.common.concurrent.locks;

import java.util.NoSuchElementException;

import org.eclipse.rdf4j.common.iteration.Iteration;
import org.eclipse.rdf4j.common.iteration.IterationWrapper;

/**
 * An Iteration that holds on to a lock until the Iteration is closed. Upon
 * closing, the underlying Iteration is closed before the lock is released. This
 * iterator closes itself as soon as all elements have been read.
 */
public class LockingIteration<E, X extends Exception> extends IterationWrapper<E, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The lock to release when the Iteration is closed.
	 */
	private final Lock lock;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new LockingIteration.
	 * 
	 * @param lock
	 *        The lock to release when the itererator is closed, must not be
	 *        <tt>null</tt>.
	 * @param iter
	 *        The underlying Iteration, must not be <tt>null</tt>.
	 */
	public LockingIteration(Lock lock, Iteration<? extends E, X> iter) {
		super(iter);

		assert lock != null;
		this.lock = lock;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public synchronized boolean hasNext()
		throws X
	{
		if (isClosed()) {
			return false;
		}

		if (super.hasNext()) {
			return true;
		}

		close();
		return false;
	}

	@Override
	public synchronized E next()
		throws X
	{
		if (isClosed()) {
			throw new NoSuchElementException("Iteration has been closed");
		}

		return super.next();
	}

	@Override
	public synchronized void remove()
		throws X
	{
		if (isClosed()) {
			throw new IllegalStateException();
		}

		super.remove();
	}

	@Override
	protected void handleClose()
		throws X
	{
		try {
			super.handleClose();
		}
		finally {
			synchronized (this) {
				lock.release();
			}
		}
	}
}
