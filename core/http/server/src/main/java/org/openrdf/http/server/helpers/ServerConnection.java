/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.helpers;

import static org.openrdf.http.protocol.Protocol.MAX_TIME_OUT;
import static org.openrdf.http.protocol.Protocol.TIME_OUT_UNITS;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.restlet.data.Request;

import org.openrdf.query.Query;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.base.RepositoryConnectionWrapper;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class ServerConnection extends RepositoryConnectionWrapper {

	private static Timer timeoutTimer = new Timer("Server connection reaper", true);

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The ID under which this connection is stored in its
	 * {@link ServerRepository}.
	 */
	private final String id;

	/**
	 * The set of prepared queries, stored by their ID.
	 */
	private final Map<String, Query> queries = new ConcurrentHashMap<String, Query>();

	/**
	 * The ID for the next prepared query that is {@link #storeQuery(Query)
	 * stored} in this connection.
	 */
	private final AtomicInteger nextQueryID = new AtomicInteger(ServerUtil.RANDOM.nextInt());

	/**
	 * Stores the requests that are registered with this connection.
	 */
	private final Set<Request> requests = new HashSet<Request>();

	/**
	 * The time this connection was last accessed.
	 */
	private volatile long lastAccessed = System.currentTimeMillis();

	private CacheInfo cacheInfo;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ServerConnection(ServerRepository repository, RepositoryConnection delegate, String id) {
		super(repository, delegate);
		this.id = id;
		// Use repository's cache info while in auto-commit mode
		this.cacheInfo = repository.getCacheInfo();
	}

	/*---------*
	 * Methods *
	 *---------*/

	public String getID() {
		return id;
	}

	@Override
	public ServerRepository getRepository() {
		return (ServerRepository)super.getRepository();
	}

	@Override
	public void close()
		throws StoreException
	{
		getRepository().removeConnection(getID());
		super.close();
	}

	public String storeQuery(Query query) {
		String id = Integer.toHexString(nextQueryID.getAndIncrement());
		queries.put(id, query);
		return id;
	}

	public Query getQuery(String id) {
		return queries.get(id);
	}

	public void removeQuery(String id) {
		queries.remove(id);
	}

	/**
	 * Registers a request with this connection, preventing the connection from
	 * being closed.
	 */
	public void addRequest(Request request) {
		synchronized (requests) {
			requests.add(request);
		}
	}

	/**
	 * Deregisters a request with this connection and updates the
	 * {@link #getLastAccessed() last accessed time}.
	 */
	public void removeRequest(Request request) {
		synchronized (requests) {
			requests.remove(request);

			if (requests.isEmpty()) {
				this.lastAccessed = System.currentTimeMillis();
				timeoutTimer.schedule(new TimeoutTask(), TIME_OUT_UNITS.toMillis(MAX_TIME_OUT));
			}
		}
	}

	/**
	 * Checks whether any requests are registered with this connection. If there
	 * are, the connection should not be closed.
	 */
	public boolean hasRequests() {
		synchronized (requests) {
			return !requests.isEmpty();
		}
	}

	public long getLastAccessed() {
		return lastAccessed;
	}

	@Override
	public synchronized void begin()
		throws StoreException
	{
		super.begin();
		cacheInfo = new CacheInfo(getRepository().getCacheInfo());
	}

	@Override
	public synchronized void commit()
		throws StoreException
	{
		super.commit();

		CacheInfo repoCacheInfo = getRepository().getCacheInfo();
		if (cacheInfo.getEntityTag() != repoCacheInfo.getEntityTag()) {
			// contents have changed in this transaction
			repoCacheInfo.processUpdate();
		}
		cacheInfo = repoCacheInfo;
	}

	@Override
	public synchronized void rollback()
		throws StoreException
	{
		try {
			super.rollback();
		}
		finally {
			cacheInfo = getRepository().getCacheInfo();
		}
	}

	public synchronized CacheInfo getCacheInfo() {
		return cacheInfo;
	}

	private class TimeoutTask extends TimerTask {

		@Override
		public void run() {
			try {
				if (!hasRequests() && isOpen() && System.currentTimeMillis() - getLastAccessed() > MAX_TIME_OUT) {
					try {
						if (!isAutoCommit()) {
							logger.info("Rolling back transaction for expired connection {}", id);
							rollback();
						}
					}
					finally {
						logger.info("Closing expired connection {}", getID());
						close();
					}
				}
			}
			catch (StoreException e) {
				logger.error("Failed to rollback or close expired connection", e);
			}
		}
	}
}
