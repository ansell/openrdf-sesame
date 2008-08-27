/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail;

import info.aduna.iteration.ExceptionConvertingIteration;
import info.aduna.iteration.Iteration;

import org.openrdf.StoreException;

/**
 * @author Herko ter Horst
 */
class SailCloseableIteration<E> extends ExceptionConvertingIteration<E, StoreException> {

	public SailCloseableIteration(Iteration<? extends E, ? extends StoreException> iter) {
		super(iter);
	}

	@Override
	protected StoreException convert(Exception e)
	{
		if (e instanceof StoreException) {
			return new StoreException(e);
		}
		else if (e instanceof RuntimeException) {
			throw (RuntimeException)e;
		}
		else if (e == null) {
			throw new IllegalArgumentException("e must not be null");
		}
		else {
			throw new IllegalArgumentException("Unexpected exception type: " + e.getClass());
		}
	}
}
