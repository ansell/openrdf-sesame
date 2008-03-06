/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.transaction.operations;

import java.util.Arrays;

import info.aduna.lang.VarargsNullValueException;

import org.openrdf.model.Resource;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * Operation that clears the whole repository.
 * 
 * @author Arjohn Kampman
 */
public class ClearOperation implements TransactionOperation {

	protected Resource[] _contexts;

	public ClearOperation(Resource... contexts) {
		setContexts(contexts);
	}

	public Resource[] getContexts() {
		return _contexts;
	}

	public void setContexts(Resource... contexts) {
		verifyContext(contexts);

		_contexts = contexts;
	}

	public void execute(SailConnection con)
		throws SailException
	{
		con.clear(getContexts());
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof ClearOperation) {
			ClearOperation o = (ClearOperation)other;
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
