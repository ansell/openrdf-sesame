/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail;

import java.io.File;

import org.openrdf.StoreException;
import org.openrdf.model.ValueFactory;

/**
 * An interface for an RDF Storage. RDF Sails can store RDF
 * statements and evaluate queries over them. Statements can be stored in named
 * contexts or in the null context. Contexts can be used to group sets of
 * statements that logically belong together, for example because they come from
 * the same source. Both URIs and bnodes can be used as context identifiers.
 * 
 * @author Arjohn Kampman
 */
public interface Sail {

	public SailMetaData getSailMetaData();

	public void setDataDir(File dataDir);

	public File getDataDir();

	/**
	 * Initializes the Sail. Care should be taken that required initialization
	 * parameters have been set before this method is called. Please consult the
	 * specific Sail implementation for information about the relevant
	 * parameters.
	 * 
	 * @throws StoreException
	 *         If the Sail could not be initialized.
	 */
	public void initialize()
		throws StoreException;

	/**
	 * Shuts down the Sail, giving it the opportunity to synchronize any stale
	 * data. Care should be taken that all initialized Sails are being shut down
	 * before an application exits to avoid potential loss of data. Once shut
	 * down, a Sail can no longer be used until it is re-initialized.
	 * 
	 * @throws StoreException
	 *         If the Sail object encountered an error or unexpected situation
	 *         internally.
	 */
	public void shutDown()
		throws StoreException;

	/**
	 * Checks whether this Sail object is writable, i.e. if the data contained in
	 * this Sail object can be changed.
	 */
	public boolean isWritable()
		throws StoreException;

	/**
	 * Opens a connection on the Sail which can be used to query and update data.
	 * Depending on how the implementation handles concurrent access, a call to
	 * this method might block when there is another open connection on this
	 * Sail.
	 * 
	 * @throws StoreException
	 *         If no transaction could be started, for example because the Sail
	 *         is not writable.
	 */
	public SailConnection getConnection()
		throws StoreException;

	/**
	 * Gets a ValueFactory object that can be used to create URI-, blank node-,
	 * literal- and statement objects.
	 * 
	 * @return a ValueFactory object for this Sail object.
	 */
	public ValueFactory getValueFactory();
}
