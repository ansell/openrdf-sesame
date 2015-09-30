/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.federation.algebra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.rdf4j.query.algebra.AbstractQueryModelNode;
import org.eclipse.rdf4j.query.algebra.QueryModelNode;
import org.eclipse.rdf4j.query.algebra.QueryModelVisitor;

/**
 * An abstract superclass for operators which have (zero or more) arguments.
 */
public abstract class AbstractNaryOperator<Expr extends QueryModelNode> extends AbstractQueryModelNode {

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

	public AbstractNaryOperator() {
		super();
	}

	/**
	 * Creates a new n-ary operator.
	 */
	public AbstractNaryOperator(final Expr... args) {
		this(Arrays.asList(args));
	}

	/**
	 * Creates a new n-ary operator.
	 */
	public AbstractNaryOperator(final List<? extends Expr> args) {
		this();
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
	public Expr getArg(final int idx) {
		return (idx < args.size()) ? args.get(idx) : null; // NOPMD
	}

	/**
	 * Sets the arguments of this n-ary tuple operator.
	 */
	private final void addArgs(final List<? extends Expr> args) {
		assert args != null;
		for (Expr arg : args) {
			addArg(arg);
		}
	}

	/**
	 * Sets the arguments of this n-ary operator.
	 */
	public final void addArg(final Expr arg) {
		setArg(this.args.size(), arg);
	}

	/**
	 * Sets the arguments of this n-ary operator.
	 */
	private final void setArgs(final List<? extends Expr> args) {
		this.args.clear();
		addArgs(args);
	}

	/**
	 * Sets the <tt>idx</tt>-th argument of this n-ary tuple operator.
	 */
	protected final void setArg(final int idx, final Expr arg) {
		if (arg != null) {
			// arg can be null (i.e. Regex)
			arg.setParentNode(this);
		}

		while (args.size() <= idx) {
			args.add(null);
		}

		this.args.set(idx, arg);
	}

	public boolean removeArg(final Expr arg) {
		return args.remove(arg);
	}

	@Override
	public <X extends Exception> void visitChildren(final QueryModelVisitor<X> visitor)
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
	public void replaceChildNode(final QueryModelNode current, final QueryModelNode replacement) {
		final int index = args.indexOf(current);
		if (index >= 0) {
			setArg(index, (Expr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public AbstractNaryOperator<Expr> clone() { // NOPMD
		final AbstractNaryOperator<Expr> clone = (AbstractNaryOperator<Expr>)super.clone();
		clone.args = new ArrayList<Expr>(args.size());
		for (Expr arg : args) {
			final Expr argClone = (arg == null) ? null : (Expr)arg.clone(); // NOPMD
			clone.addArg(argClone);
		}
		return clone;
	}
}
