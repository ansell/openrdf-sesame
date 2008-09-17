/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.transaction.operations;

import info.aduna.lang.ObjectUtil;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.StoreException;

/**
 * Operation that sets the namespace for a specific prefix.
 * 
 * @author Arjohn Kampman
 * @author Leo Sauermann
 */
public class SetNamespaceOperation implements TransactionOperation {

	private String prefix;

	private String name;

	public SetNamespaceOperation() {
	}

	public SetNamespaceOperation(String prefix, String name) {
		setPrefix(prefix);
		setName(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void execute(RepositoryConnection con)
		throws StoreException
	{
		con.setNamespace(prefix, name);
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

	@Override
	public int hashCode()
	{
		int hashCode = ObjectUtil.nullHashCode(getPrefix());
		hashCode = 31 * hashCode + ObjectUtil.nullHashCode(getName());
		return hashCode;
	}
}
