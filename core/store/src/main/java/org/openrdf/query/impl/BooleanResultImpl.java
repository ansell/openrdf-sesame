/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.impl;

import org.openrdf.query.BooleanResult;
import org.openrdf.query.Cursor;
import org.openrdf.query.SingletonCursor;
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

	public boolean asBoolean()
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
