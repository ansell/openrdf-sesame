/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.helpers;

import static org.openrdf.http.protocol.Protocol.MAX_TIME_OUT;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.restlet.data.Request;
import org.restlet.data.Tag;

import org.openrdf.query.Query;
import org.openrdf.repository.Repository;
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
	 * The time this connection was last accessed.
	 */
	private volatile long lastAccessed = System.currentTimeMillis();

	/**
	 * The ID for the next prepared query that is {@link #storeQuery(Query)
	 * stored} in this connection.
	 */
	private final AtomicInteger nextQueryID = new AtomicInteger(ServerUtil.RANDOM.nextInt());

	/**
	 * The set of prepared queries, stored by their ID.
	 */
	private final Map<String, Query> queries = new ConcurrentHashMap<String, Query>();

	/**
	 * Stores the requests that are registered with this connection.
	 */
	private final Set<Request> requests = new HashSet<Request>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ServerConnection(Repository repository, RepositoryConnection delegate, String id) {
		super(repository, delegate);
		this.id = id;
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

	public Date getLastModified() {
		Date lastModified = null;

		try {
			if (isAutoCommit()) {
				lastModified = getRepository().getLastModified();
			}
		}
		catch (StoreException e) {
			logger.warn("Unable to check auto-commit status", e);
		}

		return lastModified;
	}

	public Tag getEntityTag() {
		Tag etag = null;

		try {
			if (isAutoCommit()) {
				etag = getRepository().getEntityTag();
			}
		}
		catch (StoreException e) {
			logger.warn("Unable to check auto-commit status", e);
		}

		return etag;
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
				timeoutTimer.schedule(new TimeoutTask(), MAX_TIME_OUT);
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

	/**
	 * @return Set of req.getMethod() + " " + req.getRequestURL()
	 */
	/*	public Collection<Request> getActiveRequests() {
			synchronized (activeRequests) {
				List<String> set = new ArrayList<String>(activeRequests.size());
				for (Request req : activeRequests) {
					set.add(req.getMethod() + " " + req.getResourceRef());
				}
				return set;
			}
		}
	*/

	public long getLastAccessed() {
		return lastAccessed;
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
