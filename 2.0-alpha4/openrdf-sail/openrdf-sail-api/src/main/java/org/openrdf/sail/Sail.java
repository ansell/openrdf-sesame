/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail;


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
 */
public interface Sail {

	/**
	 * Passes an initialization parameter to the Sail object. Initialization
	 * parameters should be set before calling {@link #initialize}.
	 * 
	 * @param key The parameter key.
	 * @param value The parameter value.
	 */
	public void setParameter(String key, String value);

	/**
	 * Initializes the Sail. Parameters to be used in the initialization can be
	 * set using {@link #setParameter}.
	 *
	 * @throws SailInitializationException If the Sail could not be initialized.
	 * @throws SailInternalException If the Sail object encountered an error or
	 * unexpected situation internally.
	 */
	public void initialize()
		throws SailInitializationException;

	/**
	 * Shuts down the Sail object, giving it the opportunity to synchronize any
	 * stale data. Care should be taken that all initialized Sails are being
	 * shut down before an application exits to avoid potential loss of data.
	 * Once shut down, a Sail object can no longer be used.
	 *
	 * @throws SailInternalException If the Sail object encountered an error or
	 * unexpected situation internally.
	 */
	public void shutDown();
	
	/**
	 * Checks whether this Sail object is writable, i.e. if the data contained
	 * in this Sail object can be changed.
	 */
	public boolean isWritable();

	/**
	 * Starts a transaction that can be used to change the data in this Sail
	 * object. Depending on whether the implementation supports concurrent
	 * transactions, this method call might block when another transaction is
	 * currently active.
	 *
	 * @throws SailException If no transaction could be started, for
	 * example because the Sail is not writable.
	 * @throws SailInternalException If the Sail object encountered an error or
	 * unexpected situation internally.
	 * @see #isWritable
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
