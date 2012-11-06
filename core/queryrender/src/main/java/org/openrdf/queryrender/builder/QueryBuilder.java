/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.queryrender.builder;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.parser.ParsedQuery;

/**
 * <p>
 * Interface for a QueryBuilder which provides a simple fluent API for
 * constructing Sesame query object programmatically.
 * </p>
 * 
 * @author Michael Grove
 * @since 2.7.0
 */
public interface QueryBuilder<T extends ParsedQuery> extends SupportsGroups {

	/**
	 * Return the query constructed by this query builder
	 * 
	 * @return the query
	 */
	public T query();

	/**
	 * Specify an offset for the query
	 * 
	 * @param theOffset
	 *        the new offset
	 * @return this query builder
	 */
	public QueryBuilder<T> offset(int theOffset);

	/**
	 * Specify a limit for the query
	 * 
	 * @param theLimit
	 *        the new limit for the query
	 * @return this query builder
	 */
	public QueryBuilder<T> limit(int theLimit);

	/**
	 * Create an option sub-group
	 * 
	 * @return the new group
	 */
	public GroupBuilder<T, QueryBuilder<T>> optional();

	/**
	 * Create a new sub-group of the query
	 * 
	 * @return the new group
	 */
	public GroupBuilder<T, QueryBuilder<T>> group();

	/**
	 * Reset the state of the query builder
	 */
	public void reset();

	/**
	 * Specify that this query should use the "distinct" keyword
	 * 
	 * @return this query builder
	 */
	public QueryBuilder<T> distinct();

	/**
	 * Specify that this query should use the "reduced" keyword
	 * 
	 * @return this query builder
	 */
	public QueryBuilder<T> reduced();

	/**
	 * Add projection variables to the query
	 * 
	 * @param theNames
	 *        the names of the variables to add to the projection
	 * @return this query builder
	 */
	public QueryBuilder<T> addProjectionVar(String... theNames);

	/**
	 * Add a from clause to this query
	 * 
	 * @param theURI
	 *        the from URI
	 * @return this query builder
	 */
	public QueryBuilder<T> from(URI theURI);

	/**
	 * Add a 'from named' clause to this query
	 * 
	 * @param theURI
	 *        the graph URI
	 * @return this query builder
	 */
	public QueryBuilder<T> fromNamed(URI theURI);

	public QueryBuilder<T> addProjectionStatement(String theSubj, String thePred, String theObj);

	public QueryBuilder<T> addProjectionStatement(String theSubj, String thePred, Value theObj);

	public QueryBuilder<T> addProjectionStatement(String theSubj, URI thePred, Value theObj);

	public QueryBuilder<T> addProjectionStatement(URI theSubj, String thePred, String theObj);

	public QueryBuilder<T> addProjectionStatement(URI theSubj, URI thePred, String theObj);

	public QueryBuilder<T> addProjectionStatement(String theSubj, URI thePred, String theObj);
}
