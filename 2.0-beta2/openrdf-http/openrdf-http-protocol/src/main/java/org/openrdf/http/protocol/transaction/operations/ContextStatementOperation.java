/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.transaction.operations;

import java.util.Arrays;

import info.aduna.lang.VarargsNullValueException;

import org.openrdf.model.Resource;

/**
 * A StatementOperation with (optional) context.
 * 
 * @author Arjohn Kampman
 */
public abstract class ContextStatementOperation extends StatementOperation {

	protected Resource[] _contexts;

	public Resource[] getContexts() {
		return _contexts;
	}

	public void setContexts(Resource... contexts) {
		verifyContext(contexts);

		_contexts = contexts;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof ContextStatementOperation) {
			ContextStatementOperation o = (ContextStatementOperation)other;
			return super.equals(o) && Arrays.deepEquals(getContexts(), o.getContexts());
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		int hashCode = super.hashCode();
		hashCode = 31 * hashCode + Arrays.deepHashCode(getContexts());
		return hashCode;
	}
	
	private void verifyContext(Resource... contexts) {
		if (contexts == null) {
			throw new VarargsNullValueException();
		}
	}
}
