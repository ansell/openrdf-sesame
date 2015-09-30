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
 * An order operator that can be used to order bindings as specified by a set of
 * value expressions.
 * 
 * @author Arjohn Kampman
 */
public class Order extends UnaryTupleOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private List<OrderElem> elements = new ArrayList<OrderElem>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Order() {
	}

	public Order(TupleExpr arg) {
		super(arg);
	}

	public Order(TupleExpr arg, OrderElem... elements) {
		this(arg);
		addElements(elements);
	}

	public Order(TupleExpr arg, Iterable<OrderElem> elements) {
		this(arg);
		addElements(elements);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public List<OrderElem> getElements() {
		return elements;
	}

	public void setElements(List<OrderElem> elements) {
		this.elements = elements;
	}

	public void addElements(OrderElem... elements) {
		for (OrderElem pe : elements) {
			addElement(pe);
		}
	}

	public void addElements(Iterable<OrderElem> elements) {
		for (OrderElem pe : elements) {
			addElement(pe);
		}
	}

	public void addElement(OrderElem pe) {
		elements.add(pe);
		pe.setParentNode(this);
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
		for (OrderElem elem : elements) {
			elem.visit(visitor);
		}

		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (replaceNodeInList(elements, current, replacement)) {
			return;
		}
		super.replaceChildNode(current, replacement);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Order && super.equals(other)) {
			Order o = (Order)other;
			return elements.equals(o.getElements());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ elements.hashCode();
	}

	@Override
	public Order clone() {
		Order clone = (Order)super.clone();

		clone.elements = new ArrayList<OrderElem>(getElements().size());
		for (OrderElem elem : getElements()) {
			clone.addElement(elem.clone());
		}

		return clone;
	}
}
