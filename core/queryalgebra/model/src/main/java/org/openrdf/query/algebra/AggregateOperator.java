/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * An operator that returns aggregates values.
 * 
 * @author David Huynh
 */
public interface AggregateOperator extends ValueExpr {

	public AggregateOperator clone();
	
}
