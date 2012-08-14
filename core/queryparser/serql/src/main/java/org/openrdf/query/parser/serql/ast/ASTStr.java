/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public class ASTStr extends ASTValueExpr {

	public ASTStr(int id) {
		super(id);
	}

	public ASTStr(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public ASTValueExpr getOperand() {
		return (ASTValueExpr)children.get(0);
	}
}