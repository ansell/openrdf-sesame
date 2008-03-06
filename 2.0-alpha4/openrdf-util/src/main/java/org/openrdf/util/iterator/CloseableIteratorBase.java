/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.iterator;

import org.openrdf.util.log.ThreadLog;


/**
 * Base class for {@link CloseableIterator}s offering common functionality. This
 * class keeps track of whether the iterator has been closed and implements a
 * finalizer that makes sure the iterator is closed when being garbage
 * collected. A warning message is logged if the iterator wasn't closed properly
 * before being garbage collected. 
 */
public abstract class CloseableIteratorBase<E> implements CloseableIterator<E> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private boolean _closed = false;

	/*---------*
	 * Methods *
	 *---------*/

	// implements CloseableIterator.close()
	public void close() {
		_closed = true;
	}
	
	/**
	 * Checks whether this CloseableIterator has been closed.
	 * 
	 * @return <tt>true</tt> if the CloseableIterator has been closed,
	 * <tt>false</tt> otherwise.
	 */
	public boolean isClosed() {
		return _closed;
	}

	// extends Object.finalize()
	protected void finalize()
		throws Throwable
	{
		if (!_closed) {
			ThreadLog.warning("Closing unclosed CloseableIterator " + this +
				" due to garbage collection.");
			close();
		}
		
		super.finalize();
	}
}
