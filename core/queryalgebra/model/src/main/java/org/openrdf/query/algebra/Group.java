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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.util.iterators.Iterators;

/**
 * A tuple operator that groups tuples that have a specific set of equivalent
 * variable bindings, and that can apply aggregate functions on the grouped
 * results.
 * 
 * @author David Huynh
 * @author Arjohn Kampman
 */
public class Group extends UnaryTupleOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Set<String> groupBindings = new LinkedHashSet<String>();

	private List<GroupElem> groupElements = new ArrayList<GroupElem>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Group(TupleExpr arg) {
		super(arg);
	}

	public Group(TupleExpr arg, Iterable<String> groupBindingNames) {
		this(arg);
		setGroupBindingNames(groupBindingNames);
	}

	public Group(TupleExpr arg, Iterable<String> groupBindingNames, Iterable<GroupElem> groupElements) {
		this(arg, groupBindingNames);
		setGroupElements(groupElements);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Set<String> getGroupBindingNames() {
		return Collections.unmodifiableSet(groupBindings);
	}

	public void addGroupBindingName(String bindingName) {
		groupBindings.add(bindingName);
	}

	public void setGroupBindingNames(Iterable<String> bindingNames) {
		groupBindings.clear();
		Iterators.addAll(bindingNames.iterator(), groupBindings);
	}

	public List<GroupElem> getGroupElements() {
		return Collections.unmodifiableList(groupElements);
	}

	public void addGroupElement(GroupElem groupElem) {
		groupElements.add(groupElem);
	}

	public void setGroupElements(Iterable<GroupElem> elements) {
		this.groupElements.clear();
		Iterators.addAll(elements.iterator(), this.groupElements);
	}

	public Set<String> getAggregateBindingNames() {
		Set<String> bindings = new HashSet<String>();

		for (GroupElem binding : groupElements) {
			bindings.add(binding.getName());
		}

		return bindings;
	}

	@Override
	public Set<String> getBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>();

		bindingNames.addAll(getGroupBindingNames());
		bindingNames.addAll(getAggregateBindingNames());

		return bindingNames;
	}

	@Override
	public Set<String> getAssuredBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>();

		bindingNames.addAll(getGroupBindingNames());
		bindingNames.retainAll(getArg().getAssuredBindingNames());

		return bindingNames;
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
		super.visitChildren(visitor);

		for (GroupElem ge : groupElements) {
			ge.visit(visitor);
		}
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {

		if (replaceNodeInList(groupElements, current, replacement)) {
			return;
		}
		super.replaceChildNode(current, replacement);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Group && super.equals(other)) {
			Group o = (Group)other;
			return groupBindings.equals(o.getGroupBindingNames())
					&& groupElements.equals(o.getGroupElements());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ groupBindings.hashCode() ^ groupElements.hashCode();
	}

	@Override
	public Group clone() {
		Group clone = (Group)super.clone();

		clone.groupBindings = new LinkedHashSet<String>(getGroupBindingNames());

		clone.groupElements = new ArrayList<GroupElem>(getGroupElements().size());
		for (GroupElem ge : getGroupElements()) {
			clone.addGroupElement(ge.clone());
		}

		return clone;
	}
	
	@Override
	public String getSignature() {
		StringBuilder b = new StringBuilder();
		b.append(this.getClass().getSimpleName());
		b.append(" (");
		
		Set<String> bindingNames = getGroupBindingNames();
		int count = 0;
		for (String name: bindingNames) {
			b.append(name);
			count++;
			if (count < bindingNames.size()) {
				b.append(", ");
			}
		}
		b.append(")");
		return b.toString();
	}
}
