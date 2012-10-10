/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import java.util.List;

/**
 * A representation of a variable-binding query result as a sequence of
 * {@link BindingSet} objects. Each query result consists of zero or more
 * solutions, each of which represents a single query solution as a set of
 * bindings. Note: take care to always close a TupleQueryResult after use to
 * free any resources it keeps hold of.
 * 
 * @author jeen
 */
public interface TupleQueryResult extends QueryResult<BindingSet> {

	/**
	 * Gets the names of the bindings, in order of projection.
	 * 
	 * @return The binding names, in order of projection.
	 * @throws QueryEvaluationException
	 */
	public List<String> getBindingNames()
		throws QueryEvaluationException;
}
