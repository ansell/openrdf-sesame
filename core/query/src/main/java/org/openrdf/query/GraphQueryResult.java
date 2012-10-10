/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import java.util.Map;

import org.openrdf.model.Graph;
import org.openrdf.model.Statement;

/**
 * A representation of a query result as a sequence of {@link Statement}
 * objects. Each query result consists of zero or more Statements and
 * additionaly carries information about relevant namespace declarations. Note:
 * take care to always close a GraphQueryResult after use to free any resources
 * it keeps hold of.
 * 
 * @author Jeen Broekstra
 */
public interface GraphQueryResult extends QueryResult<Statement> {

	/**
	 * Retrieves relevant namespaces from the query result.
	 * 
	 * @return a Map<String, String> object containing (prefix, namespace) pairs.
	 * @throws QueryEvaluationException
	 */
	public Map<String, String> getNamespaces()
		throws QueryEvaluationException;

	/**
	 * Adds all elements in the query result to a new
	 * {@link org.openrdf.model.Graph}. The QueryResult is fully consumed and
	 * closed by this method.
	 * 
	 * @since 2.7.0
	 * @return a {@link org.openrdf.model.Graph} containing all statements in the
	 *         query result.
	 * @throws QueryEvaluationException
	 */
	public Graph asGraph()
		throws QueryEvaluationException;

}
