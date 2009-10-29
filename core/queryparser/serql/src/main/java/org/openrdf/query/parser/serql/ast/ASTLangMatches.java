/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public class ASTLangMatches extends ASTBooleanExpr {

	public ASTLangMatches(int id) {
		super(id);
	}

	public ASTLangMatches(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public ASTValueExpr getLanguageTag() {
		return (ASTValueExpr)children.get(0);
	}

	public ASTValueExpr getLanguageRange() {
		return (ASTValueExpr)children.get(1);
	}
}