/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.algebra;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.query.algebra.TupleExpr;

/**
 * An abstract superclass for n-ary tuple operators which have one or more
 * arguments.
 */
public abstract class AbstractNaryTupleOperator extends AbstractNaryOperator<TupleExpr> implements TupleExpr {

	private static final long serialVersionUID = 4703129201150946366L;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public AbstractNaryTupleOperator() {
		super();
	}

	/**
	 * Creates a new n-ary tuple operator.
	 */
	public AbstractNaryTupleOperator(TupleExpr... args) {
		super(args);
	}

	/**
	 * Creates a new n-ary tuple operator.
	 */
	public AbstractNaryTupleOperator(List<? extends TupleExpr> args) {
		super(args);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Set<String> getBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>(16);

		for (TupleExpr arg : getArgs()) {
			bindingNames.addAll(arg.getBindingNames());
		}

		return bindingNames;
	}

	public Set<String> getAssuredBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>(16);

		for (TupleExpr arg : getArgs()) {
			bindingNames.addAll(arg.getAssuredBindingNames());
		}

		return bindingNames;
	}

	@Override
	public AbstractNaryTupleOperator clone() { // NOPMD
		return (AbstractNaryTupleOperator)super.clone();
	}
}
