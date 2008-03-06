/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail;

import java.io.File;

import org.openrdf.model.ValueFactory;

/**
 * An interface for an RDF Storage And Inference Layer. RDF Sails can store RDF
 * statements and evaluate queries over them. Statements can be stored in named
 * contexts or in the null context. Contexts can be used to group sets of
 * statements that logically belong together, for example because they come from
 * the same source. Both URIs and bnodes can be used as context identifiers.
 * <p>
 * Most methods in this interface can throw {@link SailInternalException}s (a
 * RuntimeException) to indicate an error or an unexpected situation in the Sail
 * object internally (e.g. the database to connect to does not exist).
 *
 * @author Arjohn Kampman
 */
public interface Sail {

	/**
	 * Key used to specify a data directory in the initialization parameters.
	 */
	public static final String DATA_DIR_KEY = "dir";
	
	public void setDataDir(File dataDir);
	
	public File getDataDir();
	
	/**
	 * Passes an initialization parameter to the Sail. Initialization parameters
	 * should be set before calling {@link #initialize}.
	 * 
	 * @param key
	 *        The parameter key.
	 * @param value
	 *        The parameter value.
	 */
	public void setParameter(String key, String value);

	/**
	 * Initializes the Sail. Parameters to be used in the initialization can be
	 * set using {@link #setParameter}.
	 * 
	 * @throws SailException If the Sail could not be initialized.
	 */
	public void initialize()
		throws SailException;

	/**
	 * Shuts down the Sail, giving it the opportunity to synchronize any stale
	 * data. Care should be taken that all initialized Sails are being shut down
	 * before an application exits to avoid potential loss of data. Once shut
	 * down, a Sail can no longer be used until it is re-initialized.
	 *
	 * @throws SailException If the Sail object encountered an error or
	 * unexpected situation internally.
	 */
	public void shutDown() throws SailException;

	/**
	 * Checks whether this Sail object is writable, i.e. if the data contained in
	 * this Sail object can be changed.
	 */
	public boolean isWritable() throws SailException;

	/**
	 * Opens a connection on the Sail which can be used to query and update data.
	 * Depending on how the implementation handles concurrent access, a call to
	 * this method might block when there is another open connection on this
	 * Sail.
	 * 
	 * @throws SailException
	 *         If no transaction could be started, for example because the Sail
	 *         is not writable.
	 */
	public SailConnection getConnection()
		throws SailException;

	/**
	 * Gets a ValueFactory object that can be used to create URI-, blank node-,
	 * literal- and statement objects.
	 * 
	 * @return a ValueFactory object for this Sail object.
	 */
	public ValueFactory getValueFactory();

	/**
	 * Adds the specified SailChangedListener to receive events when the data in
	 * this Sail object changes.
	 */
	public void addSailChangedListener(SailChangedListener listener);

	/**
	 * Removes the specified SailChangedListener so that it no longer receives
	 * events from this Sail object.
	 */
	public void removeSailChangedListener(SailChangedListener listener);
}
