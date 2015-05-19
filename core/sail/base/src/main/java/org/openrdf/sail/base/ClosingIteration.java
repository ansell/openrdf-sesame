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
package org.openrdf.sail.base;

import java.util.NoSuchElementException;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.IterationWrapper;

import org.openrdf.sail.SailException;

/**
 * An {@link Iteration} that holds on to a {@link SailClosable} until the
 * Iteration is closed. Upon closing, the underlying Iteration is closed before
 * the lock is released. This iterator closes itself as soon as all elements
 * have been read.
 * 
 * @author James Leigh
 */
public abstract class ClosingIteration<T, X extends Exception> extends IterationWrapper<T, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The lock to release when the Iteration is closed.
	 */
	private final SailClosable[] closes;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new {@link Iteration} that automatically closes the given
	 * {@link SailClosable}s.
	 * 
	 * @param iter
	 *        The underlying Iteration, must not be <tt>null</tt>.
	 * @param closes
	 *        The {@link SailClosable}s to {@link SailClosable#close()} when the
	 *        itererator is closed.
	 */
	public ClosingIteration(CloseableIteration<? extends T, X> iter, SailClosable... closes) {
		super(iter);
		this.closes = closes;
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
	public synchronized T next()
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
				for (SailClosable closing : closes) {
					try {
						closing.close();
					}
					catch (SailException e) {
						handleSailException(e);
					}
				}
			}
		}
	}

	protected abstract void handleSailException(SailException e)
		throws X;
}
