/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

import java.util.List;

import info.aduna.collections.CastingList;

public class ASTFunctionCall extends ASTValueExpr {

	public ASTFunctionCall(int id) {
		super(id);
	}

	public ASTFunctionCall(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. */
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public ASTValue getURI() {
		return (ASTValue)children.get(0);
	}

	public List<ASTValueExpr> getArgList() {
		return new CastingList<ASTValueExpr>(children.subList(1, children.size()));
	}
}
