/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An abstract superclass for operators which have arguments.
 */
public abstract class NaryOperator<Expr extends QueryModelNode> extends QueryModelNodeBase {

	private static final long serialVersionUID = 2645544440976923085L;

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The operator's arguments.
	 */
	private List<Expr> args = new ArrayList<Expr>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public NaryOperator() {
	}

	/**
	 * Creates a new nary operator.
	 */
	public NaryOperator(Expr... args) {
		setArgs(args);
	}

	/**
	 * Creates a new nary operator.
	 */
	public NaryOperator(Collection<? extends Expr> args) {
		for (Expr arg : args) {
			addArg(arg);
		}
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the arguments of this nary operator.
	 * 
	 * @return The operator's arguments.
	 */
	public List<Expr> getArgs() {
		return args;
	}

	/**
	 * Sets the arguments of this nary operator.
	 */
	public void setArgs(Expr... args) {
		this.args.clear();
		addArgs(args);
	}

	/**
	 * Sets the arguments of this nary tuple operator.
	 */
	public void addArgs(Expr... args) {
		assert args != null;
		for (Expr arg : args) {
			addArg(arg);
		}
	}

	/**
	 * Sets the arguments of this nary operator.
	 */
	public void addArg(Expr arg) {
		setArg(this.args.size(), arg);
	}

	/**
	 * Gets the number of arguments of this nary operator.
	 * 
	 * @return The number of arguments.
	 */
	public int getNumberOfArguments() {
		return args.size();
	}

	/**
	 * Gets the <tt>idx</tt>-th argument of this nary operator.
	 * 
	 * @return The operator's arguments.
	 */
	public Expr getArg(int idx) {
		if (idx >= args.size())
			return null;
		return args.get(idx);
	}

	/**
	 * Sets the <tt>idx</tt>-th argument of this nary tuple operator.
	 */
	public void setArg(int idx, Expr arg) {
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
		for (Expr arg : args) {
			if (arg != null) {
				arg.visit(visitor);
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		int index = args.indexOf(current);
		if (index >= 0) {
			setArg(index, (Expr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	public boolean removeArg(Expr arg) {
		return args.remove(arg);
	}

	@Override
	@SuppressWarnings("unchecked")
	public NaryOperator<Expr> clone() {
		NaryOperator<Expr> clone = (NaryOperator<Expr>)super.clone();

		clone.args = new ArrayList<Expr>(args.size());
		for (Expr arg : args) {
			if (arg != null) {
				clone.addArg((Expr)arg.clone());
			}
		}

		return clone;
	}
}
