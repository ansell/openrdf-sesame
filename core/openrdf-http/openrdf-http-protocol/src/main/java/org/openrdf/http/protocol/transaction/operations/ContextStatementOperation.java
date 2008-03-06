/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.transaction.operations;

import info.aduna.lang.ObjectUtil;

import org.openrdf.model.Resource;

/**
 * A StatementOperation with (optional) context.
 * 
 * @author Arjohn Kampman
 */
public abstract class ContextStatementOperation extends StatementOperation {

	protected Resource _context;

	public Resource getContext() {
		return _context;
	}

	public void setContext(Resource context) {
		_context = context;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof ContextStatementOperation) {
			ContextStatementOperation o = (ContextStatementOperation)other;
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
