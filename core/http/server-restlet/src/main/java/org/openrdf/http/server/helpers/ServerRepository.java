/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.helpers;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.restlet.data.Tag;

import org.openrdf.repository.Repository;
import org.openrdf.repository.base.RepositoryWrapper;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
public class ServerRepository extends RepositoryWrapper {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The date the repository was initialized.
	 */
	private final long initializationDate = System.currentTimeMillis();

	/**
	 * The date the repository was last modified.
	 */
	private Date lastModified = new Date();

	/**
	 * A counter that is increased with each repository update.
	 */
	private final AtomicLong contentVersion = new AtomicLong(0);

	/**
	 * A tag that can be added to served entities, changed with each repository
	 * update.
	 */
	private volatile Tag entityTag;

	/**
	 * The ID for the next connection that is opened on this repository.
	 */
	private final AtomicInteger nextConnectionID = new AtomicInteger(ServerUtil.RANDOM.nextInt());

	/**
	 * The set of open connections, stored by their ID.
	 */
	private final Map<String, ServerConnection> connections = new ConcurrentHashMap<String, ServerConnection>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ServerRepository(Repository delegate) {
		super(delegate);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Date getLastModified() {
		return lastModified;
	}

	public Tag getEntityTag() {
		Tag result = entityTag;

		if (result == null) {
			String etag = Long.toHexString(initializationDate);
			etag += Long.toHexString(contentVersion.get());
			entityTag = result = new Tag(etag);
		}

		return result;
	}

	public void markRepositoryChanged() {
		contentVersion.incrementAndGet();
		lastModified = new Date();
		entityTag = null;
	}

	/**
	 * Creates a {@link ServerConnection} and registers it under a new
	 * {@link ServerConnection#getID() ID}.
	 */
	@Override
	public ServerConnection getConnection()
		throws StoreException
	{
		String connectionID = Integer.toHexString(nextConnectionID.getAndIncrement());
		ServerConnection connection = new ServerConnection(this, super.getConnection(), connectionID);
		connections.put(connectionID, connection);
		return connection;
	}

	/**
	 * Gets the connection that is registered under the specified ID.
	 * 
	 * @param id
	 *        A connection ID.
	 * @return The connection for the specified ID, or <tt>null</tt> if no such
	 *         connection exists.
	 */
	public ServerConnection getConnection(String id) {
		// FIXME: prevent concurrent requests to non auto-commit connections
		return connections.get(id);
	}

	/**
	 * Removes the connection with the specified ID from this repository's
	 * registry.
	 * 
	 * @param id
	 *        A connection ID.
	 */
	void removeConnection(String id) {
		connections.remove(id);
	}

	/**
	 * Gets the IDs of all registered connections.
	 */
	public Set<String> getConnectionIDs() {
		return connections.keySet();
	}
}
