/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;


public class ASTFunctionCall extends ASTValueExpr {

	public ASTFunctionCall(int id) {
		super(id);
	}

	public ASTFunctionCall(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public ASTValue getURI() {
		return (ASTValue)children.get(0);
	}

	public ASTArgList getArgList() {
		return (ASTArgList)children.get(1);
	}
}
