/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import java.util.Map;

import org.openrdf.model.Statement;

/**
 * A representation of a query result as a sequence of {@link Statement}
 * objects. Each query result consists of zero or more Statements and
 * additionaly carries information about relevant namespace declarations. Note:
 * take care to always close a GraphQueryResult after use to free any resources
 * it keeps hold of.
 * 
 * @author jeen
 */
public interface GraphQueryResult extends QueryResult<Statement> {

	/**
	 * Retrieves relevant namespaces from the query result.
	 * 
	 * @return a Map<String, String> object containing (prefix, namespace)
	 *         pairs.
	 */
	public Map<String, String> getNamespaces();

	/**
	 * Checks if the result is guaranteed to contain no duplicate solutions.
	 * 
	 * @return true if the result is guaranteed to contain no duplicate
	 *         solutions, false otherwise.
	 * @deprecated isDistinct is no longer supported, following a decision by the
	 *             W3C RDF Data Access Working Group to remove the 'ordered' and
	 *             'distinct' flags from the SPARQL XML Query Results format. For
	 *             more information see
	 *             http://www.openrdf.org/issues/browse/SES-408
	 */
	@Deprecated
	public boolean isDistinct();
}
