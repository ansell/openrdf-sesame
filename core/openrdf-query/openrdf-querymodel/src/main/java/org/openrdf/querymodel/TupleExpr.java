/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;

import java.util.Set;

/**
 * An expression that evaluates to RDF tuples.
 */
public abstract class TupleExpr extends QueryModelNode {

	/**
	 * Gets the names of the bindings that are returned by this tuple expression
	 * when it is evaluated.
	 * 
	 * @return A set of binding names.
	 */
	public abstract Set<String> getBindingNames();
}
