/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import org.openrdf.model.Value;

/**
 * A Query is an object representation of a query on a {@link Repository} that can be
 * formulated in one of the supported query languages (for example SeRQL or
 * SPARQL). It allows one to predefine bindings in the query to be able to reuse
 * the same query with different bindings.
 * 
 * @author Arjohn Kampman
 * @author jeen
 * 
 * @see org.openrdf.repository.RepositoryConnection
 */
public interface Query {

	/**
	 * adds a binding of the supplied variable to the supplied name, to restrict
	 * results of evaluation to results matching this binding. Each variable can
	 * only be bound once; subsequent calls of this method on the same variable
	 * name will overwrite previous settings.
	 * 
	 * @param name
	 *        the name of the variable to be bound
	 * @param value
	 *        the value to which the variable is to be bound.
	 */
	public void addBinding(String name, Value value);

	/**
	 * Removes a previously set binding on the supplied variable.
	 * 
	 * @param name
	 *        the name of the variable from which the binding is to be removed.
	 */
	public void removeBinding(String name);

	/**
	 * Retrieves a {@link BindingSet} containing a {@link Binding} for each
	 * variable in the query that has been bound using the addBinding method.
	 * 
	 * @return A (possibly empty) set of query variable bindings.
	 */
	public BindingSet getBindings();

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
