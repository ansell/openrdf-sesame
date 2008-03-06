/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIterationBase;

/**
 * A Locking iteration extension that keeps a reference to the
 * SailConnectionBase from which it originates and signals when it is closed.
 * 
 * @author jeen
 */
class SailBaseIteration<T, E extends Exception> extends CloseableIterationBase<T, E> {

	private SailConnectionBase connection;

	private CloseableIteration<? extends T, ? extends E> iter;

	/**
	 * Creates a new memory-store specific iteration object.
	 * 
	 * @param lock
	 *        a query lock
	 * @param iter
	 *        the wrapped iteration over sail objects.
	 * @param connection
	 *        the connection from which this iteration originates.
	 */
	public SailBaseIteration(CloseableIteration<? extends T, ? extends E> iter,
			SailConnectionBase connection)
	{
		this.iter = iter;
		this.connection = connection;
	}

	protected void handleClose()
		throws E
	{
		iter.close();
		connection.iterationClosed(this);
		super.handleClose();
	}

	public boolean hasNext()
		throws E
	{
		return iter.hasNext();
	}

	public T next()
		throws E
	{
		return iter.next();
	}

	public void remove()
		throws E
	{
		iter.remove();
	}

}
