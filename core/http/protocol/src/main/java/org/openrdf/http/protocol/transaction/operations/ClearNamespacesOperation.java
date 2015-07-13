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

import java.io.Serializable;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Operation that removes all namespace declarations.
 * 
 * @author Arjohn Kampman
 * @author Leo Sauermann
 */
public class ClearNamespacesOperation implements TransactionOperation, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 804163331093326031L;

	public ClearNamespacesOperation() {
	}

	public void execute(RepositoryConnection con)
		throws RepositoryException
	{
		con.clearNamespaces();
	}

	@Override
	public boolean equals(Object other)
	{
		return other instanceof ClearNamespacesOperation;
	}

	@Override
	public int hashCode()
	{
		return 101;
	}
}
