/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

import java.util.List;

import info.aduna.collections.CastingList;


public class ASTOptPathExpr extends ASTPathExpr {

	public ASTOptPathExpr(int id) {
		super(id);
	}

	public ASTOptPathExpr(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. */
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public List<ASTPathExpr> getPathExprList() {
		if (this.hasConstraint()) {
			// exlude the last child, which is a boolean constraint
			return new CastingList<ASTPathExpr>(children.subList(0, children.size() - 1));
		}
		else {
			return new CastingList<ASTPathExpr>(children);
		}
	}

	/**
	 * Checks if this optional path expression has a constraint.
	 */
	public boolean hasConstraint() {
		return getWhereClause() != null;
	}

	/**
	 * Returns the where clause on the optional path expression, if present.
	 * 
	 * @return The where clause, or <tt>null</tt> if no where clause was
	 *         specified.
	 */
	public ASTWhere getWhereClause() {
		Node lastChildNode = children.get(children.size() - 1);

		if (lastChildNode instanceof ASTWhere) {
			return (ASTWhere)lastChildNode;
		}

		return null;
	}
}
