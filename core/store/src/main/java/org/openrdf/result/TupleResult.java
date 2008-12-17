/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.result;

import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.store.StoreException;

/**
 * A representation of a variable-binding query result as a sequence of
 * {@link BindingSet} objects. Each query result consists of zero or more
 * solutions, each of which represents a single query solution as a set of
 * bindings. Note: take care to always close a TupleQueryResult after use to
 * free any resources it keeps hold of.
 * 
 * @author jeen
 */
public interface TupleResult extends TupleQueryResult {

	/**
	 * Gets the names of the bindings, in order of projection.
	 * 
	 * @return The binding names, in order of projection.
	 * @throws StoreException
	 */
	public List<String> getBindingNames()
		throws StoreException;
}
