/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.Set;

/**
 * An expression that evaluates to RDF tuples.
 */
public interface TupleExpr extends QueryModelNode {

	/**
	 * Gets the names of the bindings that are returned by this tuple expression
	 * when it is evaluated.
	 * 
	 * @return A set of binding names.
	 */
	public Set<String> getBindingNames();

	public TupleExpr clone();
}
