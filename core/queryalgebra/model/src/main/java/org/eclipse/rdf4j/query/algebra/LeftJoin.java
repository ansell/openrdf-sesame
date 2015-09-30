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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The LeftJoin operator, as defined in <a
 * href="http://www.w3.org/TR/rdf-sparql-query/#algLeftJoin">SPARQL Query
 * Language for RDF</a>.
 * 
 * @author Arjohn Kampman
 */
public class LeftJoin extends BinaryTupleOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueExpr condition;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public LeftJoin() {
	}

	public LeftJoin(TupleExpr leftArg, TupleExpr rightArg) {
		super(leftArg, rightArg);
	}

	public LeftJoin(TupleExpr leftArg, TupleExpr rightArg, ValueExpr condition) {
		this(leftArg, rightArg);
		setCondition(condition);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public ValueExpr getCondition() {
		return condition;
	}

	public void setCondition(ValueExpr condition) {
		if (condition != null) {
			condition.setParentNode(this);
		}
		this.condition = condition;
	}

	public boolean hasCondition() {
		return condition != null;
	}

	public Set<String> getBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>(16);
		bindingNames.addAll(getLeftArg().getBindingNames());
		bindingNames.addAll(getRightArg().getBindingNames());
		return bindingNames;
	}

	public Set<String> getAssuredBindingNames() {
		return getLeftArg().getAssuredBindingNames();
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
		if (condition != null) {
			condition.visit(visitor);
		}

		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (condition == current) {
			setCondition((ValueExpr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof LeftJoin && super.equals(other)) {
			ValueExpr oCond = ((LeftJoin)other).getCondition();
			return nullEquals(condition, oCond);
		}

		return false;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode() ^ "LeftJoin".hashCode();
		if (hasCondition()) {
			result ^= getCondition().hashCode();
		}
		return result;
	}

	@Override
	public LeftJoin clone() {
		LeftJoin clone = (LeftJoin)super.clone();
		if (hasCondition()) {
			clone.setCondition(getCondition().clone());
		}
		return clone;
	}
}
