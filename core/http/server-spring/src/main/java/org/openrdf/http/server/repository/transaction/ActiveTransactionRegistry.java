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

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Registry keeping track of active transactions identified by a {@link UUID}
 * and the {@link RepositoryConnection} that corresponds to the given
 * transaction.
 * 
 * @author Jeen Broekstra
 * @since 2.8.0
 */
public enum ActiveTransactionRegistry {

	INSTANCE;

	private static final Logger logger = LoggerFactory.getLogger(ActiveTransactionRegistry.class);

	static class CacheEntry {

		private final RepositoryConnection connection;

		private final Lock lock = new ReentrantLock();

		public CacheEntry(RepositoryConnection connection) {
			this.connection = connection;
		}

		/**
		 * @return Returns the connection.
		 */
		public RepositoryConnection getConnection() {
			return connection;
		}

		/**
		 * @return Returns the lock.
		 */
		public Lock getLock() {
			return lock;
		}

	}

	private final Cache<UUID, CacheEntry> activeConnections = CacheBuilder.newBuilder().removalListener(
			new RemovalListener<UUID, CacheEntry>() {

				@Override
				public void onRemoval(RemovalNotification<UUID, CacheEntry> notification) {
					if (RemovalCause.EXPIRED.equals(notification.getCause())) {
						logger.warn("transaction registry item {} removed after expiry", notification.getKey());
						CacheEntry entry = notification.getValue();
						try {
							entry.getConnection().close();
						}
						catch (RepositoryException e) {
							// fall through
						}
					}
					else {
						logger.debug("transaction {} removed from registry. cause: {}", notification.getKey(),
								notification.getCause());
					}
				}
			}).expireAfterAccess(60, TimeUnit.SECONDS).build();

	/**
	 * Register a new transaction with the given id and connection.
	 * 
	 * @param transactionId
	 *        the transaction id
	 * @param conn
	 *        the {@link RepositoryConnection} to use for handling the
	 *        transaction.
	 * @throws IllegalArgumentException
	 *         if a transaction is already registered with the given transaction
	 *         id.
	 */
	public void register(UUID transactionId, RepositoryConnection conn)
		throws IllegalArgumentException
	{
		synchronized (activeConnections) {
			if (activeConnections.getIfPresent(transactionId) == null) {
				activeConnections.put(transactionId, new CacheEntry(conn));
				logger.debug("registered transaction {} ", transactionId);
			}
			else {
				logger.error("transaction already registered: {}", transactionId);
				throw new IllegalArgumentException("transaction with id " + transactionId.toString()
						+ " already registered.");
			}
		}
	}

	/**
	 * Remove the given transaction from the registry
	 * 
	 * @param transactionId
	 *        the transaction id
	 * @throws IllegalArgumentException
	 *         if no registered transaction with the given id could be found.
	 */
	public void deregister(UUID transactionId)
		throws IllegalArgumentException
	{
		synchronized (activeConnections) {
			CacheEntry entry = activeConnections.getIfPresent(transactionId);
			if (entry == null) {
				throw new IllegalArgumentException("transaction with id " + transactionId.toString()
						+ " not registered.");
			}
			else {
				activeConnections.invalidate(transactionId);
				logger.debug("deregistered transaction {}", transactionId);
			}
		}
	}

	/**
	 * Obtain the {@link RepositoryConnection} associated with the given
	 * transaction. This method will block if another thread currently has access
	 * to the connection.
	 * 
	 * @param transactionId
	 *        a transaction ID
	 * @return the RepositoryConnection belonging to this transaction.
	 * @throws IllegalArgumentException
	 *         if no transaction with the given id is registered.
	 * @throws InterruptedException
	 *         if the thread is interrupted while acquiring a lock on the
	 *         transaction.
	 */
	public RepositoryConnection getTransactionConnection(UUID transactionId)
		throws InterruptedException
	{
		Lock txnLock = null;
		synchronized (activeConnections) {
			CacheEntry entry = activeConnections.getIfPresent(transactionId);
			if (entry == null) {
				throw new IllegalArgumentException("transaction with id " + transactionId.toString()
						+ " not registered.");
			}

			txnLock = entry.getLock();
		}

		txnLock.lockInterruptibly();

		final RepositoryConnection conn = activeConnections.getIfPresent(transactionId).getConnection();

		return conn;
	}

	/**
	 * Unlocks the {@link RepositoryConnection} associated with the given
	 * transaction for use by other threads. If the transaction is no longer
	 * registered, this will method will exit silently.
	 * 
	 * @param transactionId
	 *        a transaction identifier.
	 */
	public void returnTransactionConnection(UUID transactionId) {

		final CacheEntry entry = activeConnections.getIfPresent(transactionId);

		if (entry != null) {
			final Lock txnLock = entry.getLock();
			txnLock.unlock();
		}
	}
}
