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

	/**
	 * Singleton instance
	 */
	INSTANCE;

	private final Logger logger = LoggerFactory.getLogger(ActiveTransactionRegistry.class);

	/**
	 * Configurable system property {@code sesame.server.txn.registry.timeout}
	 * for specifying the transaction cache timeout (in seconds).
	 */
	public static final String CACHE_TIMEOUT_PROPERTY = "sesame.server.txn.registry.timeout";

	/**
	 * Default timeout setting for transaction cache entries (in seconds).
	 */
	public final static int DEFAULT_TIMEOUT = 60;

	private final Cache<UUID, CacheEntry> activeConnections;

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

	/**
	 * private constructor. Access via {@link ActiveTransactionRegistry#INSTANCE}
	 */
	private ActiveTransactionRegistry() {
		int timeout = DEFAULT_TIMEOUT;

		final String configuredValue = System.getProperty(CACHE_TIMEOUT_PROPERTY);
		if (configuredValue != null) {
			try {
				timeout = Integer.parseInt(configuredValue);
			}
			catch (NumberFormatException e) {
				logger.warn("Expected integer value for property {}. Timeout will default to {} seconds. ",
						CACHE_TIMEOUT_PROPERTY, DEFAULT_TIMEOUT);
			}
		}

		activeConnections = initializeCache(timeout, TimeUnit.SECONDS);
	}

	private final Cache<UUID, CacheEntry> initializeCache(int timeout, TimeUnit unit) {
		return CacheBuilder.newBuilder().removalListener(new RemovalListener<UUID, CacheEntry>() {

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
		}).expireAfterAccess(timeout, unit).build();
	}

	/**
	 * Register a new transaction with the given id and connection.
	 * 
	 * @param transactionId
	 *        the transaction id
	 * @param conn
	 *        the {@link RepositoryConnection} to use for handling the
	 *        transaction.
	 * @throws RepositoryException
	 *         if a transaction is already registered with the given transaction
	 *         id.
	 */
	public void register(UUID transactionId, RepositoryConnection conn)
		throws RepositoryException
	{
		synchronized (activeConnections) {
			if (activeConnections.getIfPresent(transactionId) == null) {
				activeConnections.put(transactionId, new CacheEntry(conn));
				logger.debug("registered transaction {} ", transactionId);
			}
			else {
				logger.error("transaction already registered: {}", transactionId);
				throw new RepositoryException(
						"transaction with id " + transactionId.toString() + " already registered.");
			}
		}
	}

	/**
	 * Remove the given transaction from the registry
	 * 
	 * @param transactionId
	 *        the transaction id
	 * @throws RepositoryException
	 *         if no registered transaction with the given id could be found.
	 */
	public void deregister(UUID transactionId)
		throws RepositoryException
	{
		synchronized (activeConnections) {
			CacheEntry entry = activeConnections.getIfPresent(transactionId);
			if (entry == null) {
				throw new RepositoryException(
						"transaction with id " + transactionId.toString() + " not registered.");
			}
			else {
				activeConnections.invalidate(transactionId);
				final Lock txnLock = entry.getLock();
				txnLock.unlock();
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
	 * @throws RepositoryException
	 *         if no transaction with the given id is registered.
	 * @throws InterruptedException
	 *         if the thread is interrupted while acquiring a lock on the
	 *         transaction.
	 */
	public RepositoryConnection getTransactionConnection(UUID transactionId)
		throws RepositoryException, InterruptedException
	{
		Lock txnLock = null;
		synchronized (activeConnections) {
			CacheEntry entry = activeConnections.getIfPresent(transactionId);
			if (entry == null) {
				throw new RepositoryException(
						"transaction with id " + transactionId.toString() + " not registered.");
			}

			txnLock = entry.getLock();
		}

		txnLock.lockInterruptibly();
		/* Another thread might have deregistered the transaction while we were acquiring the lock */
		final CacheEntry entry = activeConnections.getIfPresent(transactionId);
		if (entry == null) {
			throw new RepositoryException("transaction with id " + transactionId + " is no longer registered!");
		}
		return entry.getConnection();
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
