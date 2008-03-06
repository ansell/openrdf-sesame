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

	private List<ExtensionElem> _elements = new ArrayList<ExtensionElem>();

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
		add(elements);
	}

	public Extension(TupleExpr arg, Iterable<ExtensionElem> elements) {
		this(arg);
		add(elements);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public List<ExtensionElem> getElements() {
		return _elements;
	}

	public void setElements(List<ExtensionElem> elements) {
		_elements = elements;
	}

	public void add(ExtensionElem pe) {
		_elements.add(pe);
		pe.setParentNode(this);
	}

	public void add(ExtensionElem... elements) {
		for (ExtensionElem pe : elements) {
			add(pe);
		}
	}

	public void add(Iterable<ExtensionElem> elements) {
		for (ExtensionElem pe : elements) {
			add(pe);
		}
	}

	public Set<String> getBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>(_arg.getBindingNames());

		for (ExtensionElem pe : _elements) {
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
		for (ExtensionElem elem : _elements) {
			elem.visit(visitor);
		}

		super.visitChildren(visitor);
	}

	public String toString() {
		return "EXTEND";
	}

	public TupleExpr cloneTupleExpr() {
		List<ExtensionElem> elements = new ArrayList<ExtensionElem>(_elements.size());
		for (ExtensionElem elem : _elements) {
			elements.add(elem.cloneExtensionElem());
		}
		return new Extension(getArg().cloneTupleExpr(), elements);
	}
}
