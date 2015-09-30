/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.openrdf.queryrender.builder;

import org.openrdf.model.IRI;
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
	public QueryBuilder<T> from(IRI theURI);

	/**
	 * Add a 'from named' clause to this query
	 * 
	 * @param theURI
	 *        the graph URI
	 * @return this query builder
	 */
	public QueryBuilder<T> fromNamed(IRI theURI);

	/**
	 * Specify ORDER BY clause with ASC modifier by default
	 * @param theNames the names of the variables to apply the ordering
	 * @return this query builder
	 */
	public QueryBuilder<T> orderBy(String... theNames);

	/**
	 * Specify ORDER BY clause with ASC modifier
	 * @param theNames the names of the variables to apply the ordering
	 * @return this query builder
	 */
	public QueryBuilder<T> orderByAsc(String... theNames);

	/**
	 * Specify ORDER BY clause with DESC modifier
	 * @param theNames the names of the variables to apply the ordering
	 * @return this query builder
	 */
	public QueryBuilder<T> orderByDesc(String... theNames);

	public QueryBuilder<T> addProjectionStatement(String theSubj, String thePred, String theObj);

	public QueryBuilder<T> addProjectionStatement(String theSubj, String thePred, Value theObj);

	public QueryBuilder<T> addProjectionStatement(String theSubj, IRI thePred, Value theObj);

	public QueryBuilder<T> addProjectionStatement(IRI theSubj, String thePred, String theObj);

	public QueryBuilder<T> addProjectionStatement(IRI theSubj, IRI thePred, String theObj);

	public QueryBuilder<T> addProjectionStatement(String theSubj, IRI thePred, String theObj);
}
