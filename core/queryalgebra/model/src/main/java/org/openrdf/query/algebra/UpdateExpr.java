/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * An expression that executes an update.
 */
public interface UpdateExpr extends QueryModelNode {

	public UpdateExpr clone();
}
