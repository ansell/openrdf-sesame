/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.algebra.base;

import org.openrdf.query.algebra.QueryModelNode;

/**
 * An SQL expression.
 * 
 * @author James Leigh
 */
public interface SqlExpr extends QueryModelNode {

	public abstract SqlExpr clone();
}
