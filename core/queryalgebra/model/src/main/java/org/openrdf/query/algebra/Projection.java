/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.Set;

/**
 * A generalized projection (allowing the bindings to be renamed) on a tuple
 * expression.
 */
public class Projection extends UnaryTupleOperator {

	private static final long serialVersionUID = -2878982614878677889L;

	/*-----------*
	 * Variables *
	 *-----------*/

	private ProjectionElemList projElemList = new ProjectionElemList();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Projection() {
	}

	public Projection(TupleExpr arg) {
		super(arg);
	}

	public Projection(TupleExpr arg, ProjectionElemList elements) {
		this(arg);
		setProjectionElemList(elements);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public ProjectionElemList getProjectionElemList() {
		return projElemList;
	}

	public void setProjectionElemList(ProjectionElemList projElemList) {
		this.projElemList = projElemList;
		projElemList.setParentNode(this);
	}

	@Override
	public Set<String> getBindingNames() {
		return projElemList.getTargetNames();
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
		projElemList.visit(visitor);
		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (projElemList == current) {
			setProjectionElemList((ProjectionElemList)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public Projection clone() {
		Projection clone = (Projection)super.clone();
		clone.setProjectionElemList(getProjectionElemList().clone());
		return clone;
	}
}
