/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.SailInitializationException;

import org.openrdf.model.ValueFactory;


/**
 * A Sesame repository that contains RDF data that can be queried and updated. A
 * Repository object operates on a (stack of) {@link Sail Sail} object(s),
 * offering a large number of developer-oriented utility methods. The behaviour
 * of the repository is determined by the Sail stack that it operates on; for
 * example, the repository will only support RDF Schema or OWL semantics if the
 * Sail stack includes an inferencer for this.
 * <p>
 * Access to the repository can be acquired by openening a connection to it.
 * This connection can then be used to query or update the contents of the
 * repository. Depending on the underlying Sail implementation, the repository
 * may or may not support multiple concurrent connections.
 * <p>
 * Please note that a repository needs to be initialized before it can be used
 * and that it should be shut down before it is discarded/garbage collected.
 * Forgetting the latter can result in loss of data (depending on the
 * implementation of the Sails being used)!
 */
public interface Repository {

	/**
	 * Initializes this repository. A repository needs to be initialized before
	 * it can be used.
	 * 
	 * @throws SailInitializationException
	 *         If the initialization failed.
	 */
	public void initialize()
		throws SailInitializationException;

	/**
	 * Shuts the repository down, releasing any resources that it keeps hold of.
	 * Once shut down, the repository can no longer be used. A repository may be
	 * reinitialized after it was shut down.
	 */
	public void shutDown()
		throws SailException;

	/**
	 * Gets the Sail object that is on top of the Sail stack that this repository
	 * operates on.
	 * 
	 * @return A Sail object.
	 */
	public Sail getSail();

	/**
	 * Checks whether this repository is writable, i.e. if the data contained in
	 * this repository can be changed. The writability of the repository is
	 * determined by the writability of the Sail that this repository operates
	 * on.
	 */
	public boolean isWritable();

	/**
	 * Gets a ValueFactory object for this Repository.
	 * 
	 * @return A repository-specific ValueFactory.
	 */
	public ValueFactory getValueFactory();

	/**
	 * Gets a {@link Connection Connection} on this repository that can be used
	 * for querying, adding/removing data and otherwise manipulating the contents
	 * of the repository.
	 * 
	 * @return A Connection object that allows operations on this repository.
	 * @throws SailException
	 *         If something went wrong during creation of the Connection with the
	 *         Sail.
	 */
	public Connection getConnection()
		throws SailException;
}
