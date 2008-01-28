/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 */
public class ProjectionElemList extends QueryModelNodeBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private List<ProjectionElem> elements = new ArrayList<ProjectionElem>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ProjectionElemList() {
	}

	public ProjectionElemList(ProjectionElem... elements) {
		addElements(elements);
	}

	public ProjectionElemList(Iterable<ProjectionElem> elements) {
		addElements(elements);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public List<ProjectionElem> getElements() {
		return elements;
	}

	public void setElements(List<ProjectionElem> elements) {
		this.elements = elements;
	}

	public void addElements(ProjectionElem... elements) {
		for (ProjectionElem pe : elements) {
			addElement(pe);
		}
	}

	public void addElements(Iterable<ProjectionElem> elements) {
		for (ProjectionElem pe : elements) {
			addElement(pe);
		}
	}

	public void addElement(ProjectionElem pe) {
		assert pe != null : "pe must not be null";
		elements.add(pe);
		pe.setParentNode(this);
	}

	public Set<String> getTargetNames() {
		Set<String> targetNames = new LinkedHashSet<String>(elements.size());

		for (ProjectionElem pe : elements) {
			targetNames.add(pe.getTargetName());
		}

		return targetNames;
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
		for (ProjectionElem pe : elements) {
			pe.visit(visitor);
		}

		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement)
	{
		int index = elements.indexOf(current);
		if (index >= 0) {
			elements.set(index, (ProjectionElem)replacement);
			replacement.setParentNode(this);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public ProjectionElemList clone() {
		ProjectionElemList clone = (ProjectionElemList)super.clone();

		List<ProjectionElem> elementsClone = new ArrayList<ProjectionElem>(getElements().size());
		for (ProjectionElem pe : getElements()) {
			elementsClone.add(pe.clone());
		}

		clone.setElements(elementsClone);

		return clone;
	}
}
