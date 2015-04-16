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

package info.aduna.iterator;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * @author MJAHale
 */
public abstract class LookAheadIterator<E> extends CloseableIteratorBase<E> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private E nextElement;
	private IOException closeException;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public LookAheadIterator() {
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the next element. Subclasses should implement this method so that it
	 * returns the next element.
	 * 
	 * @return The next element, or <tt>null</tt> if no more elements are
	 *         available.
	 */
	protected abstract E getNextElement();

	public final boolean hasNext()
	{
		lookAhead();

		return nextElement != null;
	}

	public final E next()
	{
		lookAhead();

		E result = nextElement;

		if (result != null) {
			nextElement = null;
			return result;
		}
		else {
			throw new NoSuchElementException();
		}
	}

	/**
	 * Fetches the next element if it hasn't been fetched yet and stores it in
	 * {@link #nextElement}.
	 * 
	 * @throws X
	 */
	private void lookAhead()
	{
		if (nextElement == null && !isClosed()) {
			nextElement = getNextElement();

			if (nextElement == null) {
				try
				{
					close();
				}
				catch(IOException ioe)
				{
					closeException = ioe;
				}
			}
		}
	}

	/**
	 * Throws an {@link UnsupportedOperationException}.
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void handleClose()
		throws IOException
	{
		super.handleClose();
		nextElement = null;
	}

	protected void handleAlreadyClosed()
			throws IOException
	{
		if(closeException != null)
		{
			throw closeException;
		}
	}
}
