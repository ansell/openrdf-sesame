/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An abstract superclass for binary value operators which have one or more
 * arguments.
 */
public abstract class NaryValueOperator extends QueryModelNodeBase implements ValueExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The operator's arguments.
	 */
	protected List<ValueExpr> args = new ArrayList<ValueExpr>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public NaryValueOperator() {
	}

	/**
	 * Creates a new nary tuple operator.
	 * 
	 * @param args
	 *        The operator's arguments, must not be <tt>null</tt>.
	 */
	public NaryValueOperator(ValueExpr... args) {
		setArgs(args);
	}

	/**
	 * Creates a new nary tuple operator.
	 * 
	 * @param args
	 *        The operator's arguments, must not be <tt>null</tt>.
	 */
	public NaryValueOperator(Collection<? extends ValueExpr> args) {
		this(args.toArray(new ValueExpr[args.size()]));
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the arguments of this nary tuple operator.
	 * 
	 * @return The operator's arguments.
	 */
	public ValueExpr[] getArgs() {
		return args.toArray(new ValueExpr[args.size()]);
	}

	/**
	 * Sets the arguments of this nary tuple operator.
	 * 
	 * @param args
	 *        The (new) arguments for this operator, must not be <tt>null</tt>.
	 */
	public void setArgs(ValueExpr... args) {
		this.args.clear();
		addArgs(args);
	}

	/**
	 * Sets the arguments of this nary tuple operator.
	 * 
	 * @param args
	 *        The (new) arguments for this operator, must not be <tt>null</tt>.
	 */
	public void addArgs(ValueExpr... args) {
		assert args != null;
		for (ValueExpr arg : args) {
			addArg(arg);
		}
	}

	/**
	 * Sets the arguments of this nary tuple operator.
	 * 
	 * @param args
	 *        The (new) arguments for this operator, must not be <tt>null</tt>.
	 */
	public void addArg(ValueExpr arg) {
		assert arg != null : "arg must not be null";
		arg.setParentNode(this);
		this.args.add(arg);
	}

	/**
	 * Gets the number of arguments of this nary tuple operator.
	 * 
	 * @return The number of arguments.
	 */
	public int getNumberOfArguments() {
		return args.size();
	}

	/**
	 * Gets the <tt>idx</tt>-th argument of this nary tuple operator.
	 * 
	 * @return The operator's arguments.
	 */
	public ValueExpr getArg(int idx) {
		return args.get(idx);
	}

	/**
	 * Sets the <tt>idx</tt>-th argument of this nary tuple operator.
	 * 
	 * @param arg
	 *        The (new) <tt>idx</tt>-th argument for this operator, must not be
	 *        <tt>null</tt>.
	 */
	public void setArg(int idx, ValueExpr arg) {
		assert arg != null : "arg must not be null";
		arg.setParentNode(this);
		while (args.size() <= idx) {
			args.add(null);
		}
		this.args.set(idx, arg);
	}

	/**
	 * Gets the only argument of this nary tuple operator.
	 * 
	 * @return The operator's argument.
	 */
	public ValueExpr getArg() {
		assert args.size() == 1;
		return args.get(0);
	}

	/**
	 * Sets the only argument of this nary tuple operator.
	 * 
	 * @param arg
	 *        The (new) argument for this operator, must not be <tt>null</tt>.
	 */
	public void setArg(ValueExpr arg) {
		assert arg != null : "arg must not be null";
		if (args.isEmpty()) {
			args.add(null);
		}
		assert args.size() == 1;
		arg.setParentNode(this);
		this.args.set(0, arg);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		for (ValueExpr arg : args) {
			arg.visit(visitor);
		}
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		int index = args.indexOf(current);
		if (index >= 0) {
			args.set(index, (ValueExpr)replacement);
			replacement.setParentNode(this);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	public boolean removeArg(ValueExpr arg) {
		assert arg != null;
		return args.remove(arg);
	}

	@Override
	public NaryValueOperator clone() {
		NaryValueOperator clone = (NaryValueOperator)super.clone();

		clone.args = new ArrayList<ValueExpr>(args.size());
		for (ValueExpr arg : args) {
			clone.addArg(arg.clone());
		}

		return clone;
	}
}
