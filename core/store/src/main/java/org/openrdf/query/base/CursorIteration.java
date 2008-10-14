/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.base;

import info.aduna.iteration.LookAheadIteration;

import org.openrdf.StoreException;
import org.openrdf.query.Cursor;

/**
 * An Cursor that can convert an {@link Iterator} to a {@link Cursor}.
 * 
 * @author James Leigh
 */
public abstract class CursorIteration<E> extends LookAheadIteration<E, StoreException> {

	private Cursor<? extends E> delegate;

	public CursorIteration(Cursor<? extends E> delegate) {
		this.delegate = delegate;
	}

	public String toString() {
		return delegate.toString();
	}

	@Override
	protected E getNextElement()
		throws StoreException
	{
		return delegate.next();
	}

	@Override
	protected void handleClose()
		throws StoreException
	{
		super.handleClose();
		delegate.close();
	}

}
