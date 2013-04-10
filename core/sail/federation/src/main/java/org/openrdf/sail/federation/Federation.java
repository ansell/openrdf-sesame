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
package org.openrdf.sail.federation;

import java.io.File;
import java.nio.file.Path;
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
	private Path dataDir;

    @Override
	public Path getDataDir() {
		return dataDir;
	}

    @Override
	public void setDataDir(Path dataDir) {
		this.dataDir = dataDir;
	}

    @Override
	public ValueFactory getValueFactory() {
		return ValueFactoryImpl.getInstance();
	}

    @Override
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

    @Override
	public void initialize() throws SailException {
		for (Repository member : members) {
			try {
				member.initialize();
			} catch (RepositoryException e) {
				throw new SailException(e);
			}
		}
	}

    @Override
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
    @Override
	public void execute(Runnable command) {
		executor.execute(command);
	}

    @Override
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
