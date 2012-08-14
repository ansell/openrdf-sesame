/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

import java.util.List;

public class ASTPathExprUnion extends ASTPathExpr {

	public ASTPathExprUnion(int id) {
		super(id);
	}

	public ASTPathExprUnion(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public List<ASTPathExpr> getPathExprList() {
		return new CastingList<ASTPathExpr>(children);
	}
}