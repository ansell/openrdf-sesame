/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.queryrender.builder;

import org.openrdf.query.algebra.TupleExpr;

/**
 * <p>
 * Interface for something that supports the ability to turn itself into a
 * Sesame TupleExpr.
 * </p>
 * 
 * @author Michael Grove
 * @since 2.7.0
 */
public interface SupportsExpr {

	TupleExpr expr();
}
