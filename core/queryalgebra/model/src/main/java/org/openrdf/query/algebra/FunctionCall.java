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
package org.openrdf.query.algebra;

import java.util.ArrayList;
import java.util.List;

/**
 * A call to an (external) function that operates on zero or more arguments.
 * 
 * @author Arjohn Kampman
 */
public class FunctionCall extends AbstractQueryModelNode implements ValueExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	protected String uri;

	/**
	 * The operator's argument.
	 */
	protected List<ValueExpr> args = new ArrayList<ValueExpr>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public FunctionCall() {
	}

	/**
	 * Creates a new unary value operator.
	 * 
	 * @param args
	 *        The operator's argument, must not be <tt>null</tt>.
	 */
	public FunctionCall(String uri, ValueExpr... args) {
		setURI(uri);
		addArgs(args);
	}

	public FunctionCall(String uri, Iterable<ValueExpr> args) {
		setURI(uri);
		addArgs(args);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public String getURI() {
		return uri;
	}

	public void setURI(String uri) {
		this.uri = uri;
	}

	public List<ValueExpr> getArgs() {
		return args;
	}

	public void setArgs(Iterable<ValueExpr> args) {
		this.args.clear();
		addArgs(args);
	}

	public void addArgs(ValueExpr... args) {
		for (ValueExpr arg : args) {
			addArg(arg);
		}
	}

	public void addArgs(Iterable<ValueExpr> args) {
		for (ValueExpr arg : args) {
			addArg(arg);
		}
	}

	public void addArg(ValueExpr arg) {
		assert arg != null : "arg must not be null";
		args.add(arg);
		arg.setParentNode(this);
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		for (ValueExpr arg : args) {
			arg.visit(visitor);
		}

		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (replaceNodeInList(args, current, replacement)) {
			return;
		}
		super.replaceChildNode(current, replacement);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof FunctionCall) {
			FunctionCall o = (FunctionCall)other;
			return uri.equals(o.getURI()) && args.equals(o.getArgs());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return uri.hashCode() ^ args.hashCode();
	}

	@Override
	public FunctionCall clone() {
		FunctionCall clone = (FunctionCall)super.clone();

		clone.args = new ArrayList<ValueExpr>(getArgs().size());
		for (ValueExpr arg : getArgs()) {
			clone.addArg(arg.clone());
		}

		return clone;
	}
}
