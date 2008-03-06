/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.inferencer;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * An extension of the {@link SailConnection} interface offering methods that
 * can be used by inferencers to store and remove inferred statements.
 */
public interface InferencerConnection extends SailConnection {

	/**
	 * Adds an inferred statement to a specific context.
	 * 
	 * @param subj The subject of the statement to add.
	 * @param pred The predicate of the statement to add.
	 * @param obj The object of the statement to add.
	 * @param context A resource identifying the named context to add the
	 * statement to, or <tt>null</tt> to add the statement to the null context.
	 *
	 * @throws SailException If the statement could not be added.
	 * @throws SailInternalException If the Sail object encountered an error or
	 * unexpected situation internally.
	 */
	// FIXME: remove boolean result value to enable batch-wise processing
	public boolean addInferredStatement(
		Resource subj, URI pred, Value obj, Resource context)
		throws SailException;

	/**
	 * Removes an inferred statement from a specific context.
	 * @param subj The subject of the statement that should be removed.
	 * @param pred The predicate of the statement that should be removed.
	 * @param obj The object of the statement that should be removed.
	 * @param context A resource identifying the named context to remove the
	 * statement from, or <tt>null</tt> to remove the statement from the null
	 * context.
	 *
	 * @throws SailException If the statement could not be removed.
	 * @throws SailInternalException If the Sail object encountered an error or
	 * unexpected situation internally.
	 */
	// FIXME: remove boolean result value to enable batch-wise processing
	public boolean removeInferredStatement(
		Resource subj, URI pred, Value obj, Resource context)
		throws SailException;

	/**
	 * Removes all inferred statements from all contexts.
	 *
	 * @throws SailException If the statements could not be removed.
	 * @throws SailInternalException If the Sail object encountered an error or
	 * unexpected situation internally.
	 */
	public void clearInferred()
		throws SailException;
	
	/**
	 * Removes all inferred statements from a specific context.
	 * 
	 * @param context A resource identifying the named context to remove the
	 * inferred statements from, or <tt>null</tt> to remove the inferred
	 * statements from the null context.
	 * 
	 * @throws SailException If the statements could not be removed.
	 * @throws SailInternalException If the Sail object encountered an error or
	 * unexpected situation internally.
	 */
	public void clearInferredFromContext(Resource context)
		throws SailException;
}
