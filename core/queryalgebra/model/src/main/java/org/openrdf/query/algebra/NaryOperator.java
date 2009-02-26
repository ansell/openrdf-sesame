/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * An abstract superclass for operators which have (zero or more) arguments.
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
	 * Creates a new n-ary operator.
	 */
	public NaryOperator(Expr... args) {
		setArgs(Arrays.asList(args));
	}

	/**
	 * Creates a new n-ary operator.
	 */
	public NaryOperator(List<? extends Expr> args) {
		setArgs(args);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the arguments of this n-ary operator.
	 * 
	 * @return A copy of the current argument list.
	 */
	public List<? extends Expr> getArgs() {
		return new CopyOnWriteArrayList<Expr>(args);
	}

	/**
	 * Gets the number of arguments of this n-ary operator.
	 * 
	 * @return The number of arguments.
	 */
	public int getNumberOfArguments() {
		return args.size();
	}

	/**
	 * Gets the <tt>idx</tt>-th argument of this n-ary operator.
	 * 
	 * @return The operator's arguments.
	 */
	public Expr getArg(int idx) {
		if (idx >= args.size()) {
			return null;
		}
		return args.get(idx);
	}

	/**
	 * Sets the arguments of this n-ary tuple operator.
	 */
	public void addArgs(List<? extends Expr> args) {
		assert args != null;
		for (Expr arg : args) {
			addArg(arg);
		}
	}

	/**
	 * Sets the arguments of this n-ary operator.
	 */
	public void addArg(Expr arg) {
		setArg(this.args.size(), arg);
	}

	/**
	 * Sets the arguments of this n-ary operator.
	 */
	public void setArgs(List<? extends Expr> args) {
		this.args.clear();
		addArgs(args);
	}

	/**
	 * Sets the <tt>idx</tt>-th argument of this n-ary tuple operator.
	 */
	protected void setArg(int idx, Expr arg) {
		if (arg != null) {
			// arg can be null (i.e. Regex)
			arg.setParentNode(this);
		}

		while (args.size() <= idx) {
			args.add(null);
		}

		this.args.set(idx, arg);
	}

	public boolean removeArg(Expr arg) {
		return args.remove(arg);
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

	@Override
	@SuppressWarnings("unchecked")
	public NaryOperator<Expr> clone() {
		NaryOperator<Expr> clone = (NaryOperator<Expr>)super.clone();

		clone.args = new ArrayList<Expr>(args.size());
		for (Expr arg : args) {
			Expr argClone = (arg == null) ? null : (Expr)arg.clone();
			clone.addArg(argClone);
		}

		return clone;
	}
}
