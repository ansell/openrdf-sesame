/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;


public class ASTOptPathExpr extends ASTPathExpr {

	public ASTOptPathExpr(int id) {
		super(id);
	}

	public ASTOptPathExpr(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public ASTPathExpr getPathExpr() {
		return (ASTPathExpr)children.get(0);
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
