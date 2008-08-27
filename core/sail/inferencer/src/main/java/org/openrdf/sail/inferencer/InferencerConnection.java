/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.inferencer;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.StoreException;

/**
 * An extension of the {@link SailConnection} interface offering methods that
 * can be used by inferencers to store and remove inferred statements.
 */
public interface InferencerConnection extends NotifyingSailConnection {

	/**
	 * Adds an inferred statement to a specific context.
	 * 
	 * @param subj
	 *        The subject of the statement to add.
	 * @param pred
	 *        The predicate of the statement to add.
	 * @param obj
	 *        The object of the statement to add.
	 * @param context
	 *        A resource identifying the named context to add the statement to,
	 *        or <tt>null</tt> to add the statement to the null context.
	 * @throws StoreException
	 *         If the statement could not be added.
	 */
	// FIXME: remove boolean result value to enable batch-wise processing
	public boolean addInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException;

	/**
	 * Removes an inferred statement from a specific context.
	 * 
	 * @param subj
	 *        The subject of the statement that should be removed.
	 * @param pred
	 *        The predicate of the statement that should be removed.
	 * @param obj
	 *        The object of the statement that should be removed.
	 * @param context
	 *        A resource identifying the named context to remove the statement
	 *        from, or <tt>null</tt> to remove the statement from the null
	 *        context.
	 * @throws StoreException
	 *         If the statement could not be removed.
	 */
	// FIXME: remove boolean result value to enable batch-wise processing
	public boolean removeInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException;

	/**
	 * Removes all inferred statements from the specified/all contexts. If no
	 * contexts are specified the method operates on the entire repository.
	 * 
	 * @param contexts
	 *        The context(s) from which to remove the statements. Note that this
	 *        parameter is a vararg and as such is optional. If no contexts are
	 *        supplied the method operates on the entire repository.
	 * @throws StoreException
	 *         If the statements could not be removed.
	 */
	public void clearInferred(Resource... contexts)
		throws StoreException;

	/**
	 * Flushes any pending updates to be processed and the resulting changes to
	 * be reported to registered {@link SailConnectionListener}s.
	 * 
	 * @throws StoreException
	 *         If the updates could not be processed.
	 */
	public void flushUpdates()
		throws StoreException;
}
