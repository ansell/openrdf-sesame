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

/**
 * The BOUND function, as defined in <a
 * href="http://www.w3.org/TR/rdf-sparql-query/#func-bound">SPARQL Query
 * Language for RDF</a>; checks if a variable is bound.
 * 
 * @author Arjohn Kampman
 */
public class Bound extends AbstractQueryModelNode implements ValueExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The operator's argument.
	 */
	protected Var arg;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Bound() {
	}

	public Bound(Var arg) {
		setArg(arg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the argument of this unary value operator.
	 * 
	 * @return The operator's argument.
	 */
	public Var getArg() {
		return arg;
	}

	/**
	 * Sets the argument of this unary value operator.
	 * 
	 * @param arg
	 *        The (new) argument for this operator, must not be <tt>null</tt>.
	 */
	public void setArg(Var arg) {
		assert arg != null : "arg must not be null";
		arg.setParentNode(this);
		this.arg = arg;
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
		arg.visit(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (arg == current) {
			setArg((Var)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Bound) {
			Bound o = (Bound)other;
			return arg.equals(o.getArg());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return arg.hashCode() ^ "Bound".hashCode();
	}

	@Override
	public Bound clone() {
		Bound clone = (Bound)super.clone();
		clone.setArg(getArg().clone());
		return clone;
	}
}
