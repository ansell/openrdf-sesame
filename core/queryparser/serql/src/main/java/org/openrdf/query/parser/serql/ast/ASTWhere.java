/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public class ASTWhere extends SimpleNode {

	public ASTWhere(int id) {
		super(id);
	}

	public ASTWhere(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public ASTBooleanExpr getCondition() {
		return (ASTBooleanExpr)children.get(0);
	}
}
