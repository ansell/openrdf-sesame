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
	private List<ValueExpr> args = new ArrayList<ValueExpr>();

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
		setArg(this.args.size(), arg);
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
		if (idx >= args.size())
			return null;
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
		if (arg != null) {
			// arg can be null (ie Regex)
			arg.setParentNode(this);
		}
		while (args.size() <= idx) {
			args.add(null);
		}
		this.args.set(idx, arg);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		for (ValueExpr arg : args) {
			if (arg != null) {
				arg.visit(visitor);
			}
		}
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		int index = args.indexOf(current);
		if (index >= 0) {
			setArg(index, (ValueExpr)replacement);
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
			if (arg != null) {
				clone.addArg(arg.clone());
			}
		}

		return clone;
	}
}
