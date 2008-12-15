/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.results.impl;

import org.openrdf.results.BooleanResult;
import org.openrdf.results.Cursor;
import org.openrdf.results.base.ResultImpl;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class BooleanResultImpl extends ResultImpl<Boolean> implements BooleanResult {

	public BooleanResultImpl(boolean bool) {
		super(new SingletonCursor<Boolean>(bool));
	}

	public BooleanResultImpl(Cursor<? extends Boolean> delegate) {
		super(delegate);
	}

	public Boolean asBoolean()
		throws StoreException
	{
		try {
			return next();
		}
		finally {
			close();
		}
	}

}
