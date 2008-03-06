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
 * Operation that sets the namespace for a specific prefix.
 * 
 * @author Arjohn Kampman
 */
public class SetNamespaceOperation implements TransactionOperation {

	private String _prefix;

	private String _name;

	public SetNamespaceOperation() {
	}

	public SetNamespaceOperation(String prefix, String name) {
		setPrefix(prefix);
		setName(name);
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
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
		con.setNamespace(_prefix, _name);
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof SetNamespaceOperation) {
			SetNamespaceOperation o = (SetNamespaceOperation)other;
			return ObjectUtil.nullEquals(getPrefix(), o.getPrefix())
					&& ObjectUtil.nullEquals(getName(), o.getName());
		}

		return false;
	}

	public int hashCode() {
		int hashCode = ObjectUtil.nullHashCode(getPrefix());
		hashCode = 31 * hashCode + ObjectUtil.nullHashCode(getName());
		return hashCode;
	}
}
