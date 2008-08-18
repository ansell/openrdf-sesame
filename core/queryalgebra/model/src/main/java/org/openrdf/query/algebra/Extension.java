/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * An extension operator that can be used to add bindings to solutions whose
 * values are defined by {@link ValueExpr value expressions}.
 */
public class Extension extends UnaryTupleOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private List<ExtensionElem> elements = new ArrayList<ExtensionElem>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Extension() {
	}

	public Extension(TupleExpr arg) {
		super(arg);
	}

	public Extension(TupleExpr arg, ExtensionElem... elements) {
		this(arg);
		addElements(elements);
	}

	public Extension(TupleExpr arg, Iterable<ExtensionElem> elements) {
		this(arg);
		addElements(elements);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public List<ExtensionElem> getElements() {
		return elements;
	}

	public void setElements(Iterable<ExtensionElem> elements) {
		this.elements.clear();
		addElements(elements);
	}

	public void addElements(ExtensionElem... elements) {
		for (ExtensionElem pe : elements) {
			addElement(pe);
		}
	}

	public void addElements(Iterable<ExtensionElem> elements) {
		for (ExtensionElem pe : elements) {
			addElement(pe);
		}
	}

	public void addElement(ExtensionElem pe) {
		elements.add(pe);
		pe.setParentNode(this);
	}

	@Override
	public Set<String> getBindingNames()
	{
		Set<String> bindingNames = new LinkedHashSet<String>(getArg().getBindingNames());

		for (ExtensionElem pe : elements) {
			bindingNames.add(pe.getName());
		}

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
		for (ExtensionElem elem : elements) {
			elem.visit(visitor);
		}

		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement)
	{
		int index = elements.indexOf(current);
		if (index >= 0) {
			elements.set(index, (ExtensionElem)replacement);
			replacement.setParentNode(this);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public Extension clone() {
		Extension clone = (Extension)super.clone();

		List<ExtensionElem> elementsClone = new ArrayList<ExtensionElem>(getElements().size());
		for (ExtensionElem elem : getElements()) {
			elementsClone.add(elem.clone());
		}

		clone.setElements(elementsClone);

		return clone;
	}
}
