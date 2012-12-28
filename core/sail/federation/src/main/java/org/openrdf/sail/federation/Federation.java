/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Union multiple (possibly remote) Repositories into a single RDF store.
 * 
 * @author James Leigh
 * @author Arjohn Kampman
 */
public class Federation implements Sail, Executor {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(Federation.class);
	private final List<Repository> members = new ArrayList<Repository>();
	private final ExecutorService executor = Executors.newCachedThreadPool();
	private PrefixHashSet localPropertySpace; // NOPMD
	private boolean distinct;
	private boolean readOnly;
	private File dataDir;

	public File getDataDir() {
		return dataDir;
	}

	public void setDataDir(File dataDir) {
		this.dataDir = dataDir;
	}

	public ValueFactory getValueFactory() {
		return ValueFactoryImpl.getInstance();
	}

	public boolean isWritable() throws SailException {
		return !isReadOnly();
	}

	public void addMember(Repository member) {
		members.add(member);
	}

	/**
	 * @return PrefixHashSet or null
	 */
	public PrefixHashSet getLocalPropertySpace() {
		return localPropertySpace;
	}

	public void setLocalPropertySpace(Collection<String> localPropertySpace) { // NOPMD
		if (localPropertySpace.isEmpty()) {
			this.localPropertySpace = null; // NOPMD
		} else {
			this.localPropertySpace = new PrefixHashSet(localPropertySpace);
		}
	}

	public boolean isDistinct() {
		return distinct;
	}

	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public void initialize() throws SailException {
		for (Repository member : members) {
			try {
				member.initialize();
			} catch (RepositoryException e) {
				throw new SailException(e);
			}
		}
	}

	public void shutDown() throws SailException {
		for (Repository member : members) {
			try {
				member.shutDown();
			} catch (RepositoryException e) {
				throw new SailException(e);
			}
		}
		executor.shutdown();
	}

	/**
	 * Required by {@link java.util.concurrent.Executor Executor} interface.
	 */
	public void execute(Runnable command) {
		executor.execute(command);
	}

	public SailConnection getConnection() throws SailException {
		List<RepositoryConnection> connections = new ArrayList<RepositoryConnection>(
				members.size());
		try {
			for (Repository member : members) {
				connections.add(member.getConnection());
			}
			return readOnly ? new ReadOnlyConnection(this, connections)
					: new WritableConnection(this, connections);
		} catch (RepositoryException e) {
			closeAll(connections);
			throw new SailException(e);
		} catch (RuntimeException e) {
			closeAll(connections);
			throw e;
		}
	}

	private void closeAll(Iterable<RepositoryConnection> connections) {
		for (RepositoryConnection con : connections) {
			try {
				con.close();
			} catch (RepositoryException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}
}
