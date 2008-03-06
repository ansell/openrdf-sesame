/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.transaction.operations;

import info.aduna.lang.ObjectUtil;

import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * Operation that removes the namespace for a specific prefix.
 * 
 * @author Arjohn Kampman
 */
public class RemoveNamespaceOperation implements TransactionOperation {

	private String _prefix;

	public RemoveNamespaceOperation() {
	}

	public RemoveNamespaceOperation(String prefix) {
		setPrefix(prefix);
	}

	public String getPrefix() {
		return _prefix;
	}

	public void setPrefix(String prefix) {
		_prefix = prefix;
	}

	public void execute(SailConnection con)
		throws SailException
	{
		con.removeNamespace(_prefix);
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof RemoveNamespaceOperation) {
			RemoveNamespaceOperation o = (RemoveNamespaceOperation)other;
			return ObjectUtil.nullEquals(getPrefix(), o.getPrefix());
		}

		return false;
	}

	public int hashCode() {
		return ObjectUtil.nullHashCode(getPrefix());
	}
}
