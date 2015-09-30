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

/**
 * A tuple operator that groups tuples that have a specific set of equivalent
 * variable bindings, and that can apply aggregate functions on the grouped
 * results.
 * 
 * @author David Huynh
 * @author Arjohn Kampman
 */
public class GroupElem extends AbstractQueryModelNode {

	/*-----------*
	 * Variables *
	 *-----------*/

	private String name;

	private AggregateOperator operator;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public GroupElem(String name, AggregateOperator operator) {
		setName(name);
		setOperator(operator);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public String getName() {
		return name;
	}

	public void setName(String name) {
		assert name != null : "name must not be null";
		this.name = name;
	}

	public AggregateOperator getOperator() {
		return operator;
	}

	public void setOperator(AggregateOperator operator) {
		assert operator != null : "operator must not be null";
		this.operator = operator;
		operator.setParentNode(this);
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
		operator.visit(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (operator == current) {
			setOperator((AggregateOperator)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof GroupElem) {
			GroupElem o = (GroupElem)other;
			return name.equals(o.getName()) && operator.equals(o.getOperator());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return name.hashCode() ^ operator.hashCode();
	}

	@Override
	public GroupElem clone() {
		GroupElem clone = (GroupElem)super.clone();
		clone.setOperator(getOperator().clone());
		return clone;
	}
}
