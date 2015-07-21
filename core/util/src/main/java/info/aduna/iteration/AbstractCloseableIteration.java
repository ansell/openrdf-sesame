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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Base class for {@link CloseableIteration}s offering common functionality.
 * This class keeps track of whether the iteration has been closed and handles
 * multiple calls to {@link #close()} by ignoring all but the first call.
 */
public abstract class AbstractCloseableIteration<E, X extends Exception> implements CloseableIteration<E, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * Flag indicating whether this iteration has been closed.
	 */
	private final AtomicBoolean closed = new AtomicBoolean(false);

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Checks whether this CloseableIteration has been closed.
	 * 
	 * @return <tt>true</tt> if the CloseableIteration has been closed,
	 *         <tt>false</tt> otherwise.
	 */
	public final boolean isClosed() {
		return closed.get();
	}

	/**
	 * Calls {@link #handleClose()} upon first call and makes sure this method
	 * gets called only once.
	 */
	public final void close()
		throws X
	{
		if (closed.compareAndSet(false, true)) {
			handleClose();
		}
	}

	/**
	 * Called by {@link #close} when it is called for the first time. This method
	 * is only called once on each iteration. By default, this method does
	 * nothing.
	 * 
	 * @throws X
	 */
	protected void handleClose()
		throws X
	{
	}
}
