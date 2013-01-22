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

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * An update operation that is part of a transaction.
 * 
 * @author Arjohn Kampman
 * @author Leo Sauermann
 */
public interface TransactionOperation {

	/**
	 * Executes this operation on the supplied connection.
	 * 
	 * @param con
	 *        The connection the operation should be performed on.
	 * @throws RepositoryException
	 *         If such an exception is thrown by the connection while executing
	 *         the operation.
	 */
	public abstract void execute(RepositoryConnection con)
		throws RepositoryException;
}
