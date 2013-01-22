/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
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
