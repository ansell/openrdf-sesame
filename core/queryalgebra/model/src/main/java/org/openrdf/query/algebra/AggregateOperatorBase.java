/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * Base class for shared functionality of aggregate operators (e.g. DISTINCT
 * setting)
 * 
 * @author Jeen Broekstra
 */
public abstract class AggregateOperatorBase extends UnaryValueOperator implements AggregateOperator {

	private boolean distinct = false;

	public AggregateOperatorBase(ValueExpr arg) {
		this(arg, false);
	}

	public AggregateOperatorBase(ValueExpr arg, boolean distinct) {
		super();
		if (arg != null) {
			setArg(arg);
		}
		setDistinct(distinct);
	}

	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	public boolean isDistinct() {
		return this.distinct;
	}

	@Override
	public AggregateOperatorBase clone() {
		return (AggregateOperatorBase)super.clone();
	}

}
