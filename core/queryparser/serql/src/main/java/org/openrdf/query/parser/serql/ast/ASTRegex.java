/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public class ASTRegex extends ASTBooleanExpr {

	public ASTRegex(int id) {
		super(id);
	}

	public ASTRegex(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public ASTValueExpr getText() {
		return (ASTValueExpr)children.get(0);
	}

	public ASTValueExpr getPattern() {
		return (ASTValueExpr)children.get(1);
	}

	public boolean hasFlags() {
		return getFlags() != null;
	}

	public ASTValueExpr getFlags() {
		if (children.size() >= 3) {
			return (ASTValueExpr)children.get(2);
		}

		return null;
	}
}