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

package info.aduna.iterator;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * @author MJAHale
 */
public abstract class LookAheadIterator<E> extends AbstractCloseableIterator<E> {

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
