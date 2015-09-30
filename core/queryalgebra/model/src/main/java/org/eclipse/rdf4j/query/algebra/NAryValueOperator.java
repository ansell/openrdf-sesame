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
package org.eclipse.rdf4j.query.algebra;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract superclass for N-ary value operators.
 * 
 * @author Jeen
 */
public abstract class NAryValueOperator extends AbstractQueryModelNode implements ValueExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The operator's arguments.
	 */
	protected List<ValueExpr> args;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public NAryValueOperator() {
	}

	/**
	 * Creates a new N-Ary value operator.
	 * 
	 * @param args
	 *        The operator's list of arguments, must not be <tt>null</tt>.
	 */
	public NAryValueOperator(List<ValueExpr> args) {
		setArguments(args);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setArguments(List<ValueExpr> args) {
		this.args = args;
	}
	
	public List<ValueExpr> getArguments() {
		return this.args;
	}
	
	public void addArgument(ValueExpr arg) {
		if (args == null) {
			args = new ArrayList<ValueExpr>();
		}
		args.add(arg);
		arg.setParentNode(this);
	}
	
	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		for(ValueExpr arg: args) {
			arg.visit(visitor);
		}
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		
		boolean replaced = false;
		
		for (int i =0 ; i < args.size(); i++ ) {
			ValueExpr arg = args.get(i);
			if (arg == current) {
				args.remove(i);
				args.add(i, (ValueExpr)replacement);
				replaced = true;
			}
		}
		
		if (!replaced) {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof NAryValueOperator) {
			NAryValueOperator o = (NAryValueOperator)other;
			
			return getArguments().equals(o.getArguments());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return getArguments().hashCode();
	}

	@Override
	public NAryValueOperator clone() {
		NAryValueOperator clone = (NAryValueOperator)super.clone();
		
		clone.setArguments(new ArrayList<ValueExpr>());
		
		for(ValueExpr arg: getArguments()) {
			clone.addArgument(arg.clone());
		}
		
		return clone;
	}
}
