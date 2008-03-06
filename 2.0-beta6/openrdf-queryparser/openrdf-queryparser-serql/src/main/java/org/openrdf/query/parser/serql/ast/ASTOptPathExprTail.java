/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public class ASTOptPathExprTail extends ASTPathExprTail {

	public ASTOptPathExprTail(int id) {
		super(id);
	}

	public ASTOptPathExprTail(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. */
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	/**
	 * Gets the optional tail part of the path expression.
	 * 
	 * @return The optional tail part of the path expression.
	 */
	public ASTBasicPathExprTail getOptionalTail() {
		return (ASTBasicPathExprTail)children.get(0);
	}

	public boolean hasWhereClause() {
		return getWhereClause() != null;
	}

	/**
	 * Gets the where-clause that constrains the results of the optional path
	 * expression tail, if any.
	 * 
	 * @return The where-clause, or <tt>null</tt> if not available.
	 */
	public ASTWhere getWhereClause() {
		if (children.size() >= 2) {
			Node node = children.get(1);

			if (node instanceof ASTWhere) {
				return (ASTWhere)node;
			}
		}

		return null;
	}

	public ASTPathExprTail getNextTail() {
		if (children.size() >= 2) {
			Node node = children.get(children.size() - 1);

			if (node instanceof ASTPathExprTail) {
				return (ASTPathExprTail)node;
			}
		}

		return null;
	}
}
