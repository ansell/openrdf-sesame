/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail;

import org.openrdf.store.StoreException;

/**
 * An interface for an RDF Storage And Inference Layer. RDF Sails can store RDF
 * statements and evaluate queries over them. Statements can be stored in named
 * contexts or in the null context. Contexts can be used to group sets of
 * statements that logically belong together, for example because they come from
 * the same source. Both URIs and bnodes can be used as context identifiers.
 * 
 * @author James Leigh
 */
public interface NotifyingSail extends Sail {

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
	public NotifyingSailConnection getConnection()
		throws StoreException;

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
