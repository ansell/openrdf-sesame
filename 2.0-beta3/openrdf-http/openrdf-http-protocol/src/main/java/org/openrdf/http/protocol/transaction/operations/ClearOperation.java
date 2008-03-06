/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.transaction.operations;

import org.openrdf.model.Resource;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * Operation that clears the whole repository.
 * 
 * @author Arjohn Kampman
 */
public class ClearOperation extends ContextOperation {

	public ClearOperation(Resource... contexts) {
		super(contexts);
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
			return super.equals(other);
		}

		return false;
	}
}
