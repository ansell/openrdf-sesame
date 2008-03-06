/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.transaction.operations;

import java.util.Arrays;

import info.aduna.lang.VarargsNullValueException;

import org.openrdf.model.Resource;

/**
 * A TransactionOperation that operates on a specific (set of) contexts.
 * 
 * @author Arjohn Kampman
 */
public abstract class ContextOperation implements TransactionOperation {

	protected Resource[] _contexts;

	protected ContextOperation(Resource... contexts) {
		setContexts(contexts);
	}

	public Resource[] getContexts() {
		return _contexts;
	}

	public void setContexts(Resource... contexts) {
		if (contexts == null) {
			throw new VarargsNullValueException();
		}

		_contexts = contexts;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof ContextOperation) {
			ContextOperation o = (ContextOperation)other;
			return Arrays.deepEquals(getContexts(), o.getContexts());
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		return Arrays.deepHashCode(getContexts());
	}
}
