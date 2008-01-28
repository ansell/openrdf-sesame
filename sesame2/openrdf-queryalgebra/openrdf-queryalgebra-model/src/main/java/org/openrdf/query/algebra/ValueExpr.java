/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * An expressions that evaluates to Value objects.
 */
public interface ValueExpr extends QueryModelNode {

	public ValueExpr clone();
}
