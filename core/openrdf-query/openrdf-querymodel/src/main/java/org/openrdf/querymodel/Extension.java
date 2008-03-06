/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;

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

	private List<ExtensionElem> _elements;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Extension(TupleExpr arg) {
		super(arg);
		_elements = new ArrayList<ExtensionElem>();
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

	public void add(ExtensionElem pe) {
		_elements.add(pe);
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

	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}
	
	@Override
	public void visitChildren(QueryModelVisitor visitor) {
		for (ExtensionElem elem : _elements) {
			elem.visit(visitor);
		}

		super.visitChildren(visitor);
	}

	public String toString() {
		return "EXTEND";
	}
}
