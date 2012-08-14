/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import org.openrdf.model.Value;

/**
 * An operation (e.g. a query or an update) on a
 * {@link org.openrdf.repository.Repository} that can be formulated in one of
 * the supported query languages (for example SeRQL or SPARQL). It allows one to
 * predefine bindings in the operation to be able to reuse the same operation
 * with different bindings.
 * 
 * @author Jeen
 */
public interface Operation {

	/**
	 * Binds the specified variable to the supplied value. Any value that was
	 * previously bound to the specified value will be overwritten.
	 * 
	 * @param name
	 *        The name of the variable that should be bound.
	 * @param value
	 *        The (new) value for the specified variable.
	 */
	public void setBinding(String name, Value value);

	/**
	 * Removes a previously set binding on the supplied variable. Calling this
	 * method with an unbound variable name has no effect.
	 * 
	 * @param name
	 *        The name of the variable from which the binding is to be removed.
	 */
	public void removeBinding(String name);

	/**
	 * Removes all previously set bindings.
	 */
	public void clearBindings();

	/**
	 * Retrieves the bindings that have been set on this operation.
	 * 
	 * @return A (possibly empty) set of operation variable bindings.
	 * @see #setBinding(String, Value)
	 */
	public BindingSet getBindings();

	/**
	 * Specifies the dataset against which to execute an operation, overriding
	 * any dataset that is specified in the operation itself.
	 */
	public void setDataset(Dataset dataset);

	/**
	 * Gets the dataset that has been set using {@link #setDataset(Dataset)}, if
	 * any.
	 */
	public Dataset getDataset();

	/**
	 * Determine whether evaluation results of this operation should include
	 * inferred statements (if any inferred statements are present in the
	 * repository). The default setting is 'true'.
	 * 
	 * @param includeInferred
	 *        indicates whether inferred statements should be included in the
	 *        result.
	 */
	public void setIncludeInferred(boolean includeInferred);

	/**
	 * Returns whether or not this operation will return inferred statements (if
	 * any are present in the repository).
	 * 
	 * @return <tt>true</tt> if inferred statements will be returned,
	 *         <tt>false</tt> otherwise.
	 */
	public boolean getIncludeInferred();

}
