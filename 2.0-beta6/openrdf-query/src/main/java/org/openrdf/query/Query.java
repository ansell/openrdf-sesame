/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import org.openrdf.model.Value;

/**
 * A query on a {@link Repository} that can be formulated in one of the
 * supported query languages (for example SeRQL or SPARQL). It allows one to
 * predefine bindings in the query to be able to reuse the same query with
 * different bindings.
 * 
 * @author Arjohn Kampman
 * @author jeen
 * @see org.openrdf.repository.RepositoryConnection
 */
public interface Query {

	/**
	 * @deprecated Use {@link #setBinding(String, Value) instead.
	 */
	@Deprecated
	void addBinding(String name, Value value);

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
	 * Retrieves the bindings that have been set on this query.
	 * 
	 * @return A (possibly empty) set of query variable bindings.
	 * @see #setBinding(String, Value)
	 */
	public BindingSet getBindings();

	/**
	 * Specifies the dataset against which to evaluate a query, overriding any
	 * dataset that is specified in the query itself.
	 */
	public void setDataset(Dataset dataset);

	/**
	 * Gets the dataset that has been set using {@link #setDataset(Dataset)}, if
	 * any.
	 */
	public Dataset getDataset();

	/**
	 * Determine whether evaluation results of this query should include inferred
	 * statements (if any inferred statements are present in the repository). The
	 * default setting is 'true'.
	 * 
	 * @param includeInferred
	 *        indicates whether inferred statements should included in the
	 *        result.
	 */
	public void setIncludeInferred(boolean includeInferred);

	/**
	 * Returns whether or not this query will return inferred statements (if any
	 * are present in the repository).
	 * 
	 * @return <tt>true</tt> if inferred statements will be returned,
	 *         <tt>false</tt> otherwise.
	 */
	public boolean getIncludeInferred();
}
