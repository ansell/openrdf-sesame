/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.http.protocol.transaction.operations;

import info.aduna.lang.ObjectUtil;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

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
		throws RepositoryException
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
