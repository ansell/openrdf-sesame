/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.openrdf.http.protocol.transaction.operations;

import java.io.Serializable;

import info.aduna.lang.ObjectUtil;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Operation that sets the namespace for a specific prefix.
 * 
 * @author Arjohn Kampman
 * @author Leo Sauermann
 */
public class SetNamespaceOperation implements TransactionOperation, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7197096029612751574L;

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
