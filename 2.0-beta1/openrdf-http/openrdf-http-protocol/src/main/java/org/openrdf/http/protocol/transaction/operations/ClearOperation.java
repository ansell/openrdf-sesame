/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.transaction.operations;

import info.aduna.lang.ObjectUtil;

import org.openrdf.model.Resource;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * Operation that clears the whole repository.
 * 
 * @author Arjohn Kampman
 */
public class ClearOperation implements TransactionOperation {

	protected Resource _context;

	public ClearOperation(Resource context) {
		_context = context;
	}

	public Resource getContext() {
		return _context;
	}

	public void setContext(Resource context) {
		_context = context;
	}

	public void execute(SailConnection con)
		throws SailException
	{
		con.clear(_context);
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof ClearOperation) {
			ClearOperation o = (ClearOperation)other;
			return super.equals(o) && ObjectUtil.nullEquals(getContext(), o.getContext());
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		int hashCode = super.hashCode();
		hashCode = 31 * hashCode + ObjectUtil.nullHashCode(getContext());
		return hashCode;
	}
}
