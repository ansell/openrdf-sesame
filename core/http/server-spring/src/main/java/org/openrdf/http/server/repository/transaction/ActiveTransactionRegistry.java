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
package org.openrdf.http.server.repository.transaction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.openrdf.repository.RepositoryConnection;

/**
 * @author jeen
 */
public class ActiveTransactionRegistry {

	private static final Map<UUID, RepositoryConnection> activeConnections = new HashMap<UUID, RepositoryConnection>();

	private static final Set<UUID> checkedOutTransactions = new HashSet<UUID>();

	public static void register(UUID transactionId, RepositoryConnection conn)
		throws IllegalArgumentException
	{
		synchronized (activeConnections) {
			if (activeConnections.containsKey(transactionId)) {
				throw new IllegalArgumentException("transaction with id " + transactionId.toString()
						+ " already registered.");
			}
			activeConnections.put(transactionId, conn);
		}
	}

	public static void deregister(UUID transactionId, RepositoryConnection conn)
		throws IllegalArgumentException
	{
		synchronized (activeConnections) {
			if (!activeConnections.containsKey(transactionId)) {
				throw new IllegalArgumentException("transaction with id " + transactionId.toString()
						+ " not registered.");
			}
			activeConnections.remove(transactionId);
		}
	}

	public static RepositoryConnection getTransactionConnection(UUID transactionId)
		throws InterruptedException
	{

		RepositoryConnection conn = null;

		synchronized (activeConnections) {
			while (checkedOutTransactions.contains(transactionId)) {
				// TODO limit?
				Thread.sleep(60);
			}

			synchronized (checkedOutTransactions) {
				conn = activeConnections.get(transactionId);
				checkedOutTransactions.add(transactionId);
			}
		}

		return conn;
	}

	public static void returnTransactionConnection(UUID transactionId) {
		synchronized (checkedOutTransactions) {
			checkedOutTransactions.remove(transactionId);
		}
	}
}
