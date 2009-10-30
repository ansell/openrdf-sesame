/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

import java.util.List;

import info.aduna.collections.CastingList;

public class ASTPathExprList extends ASTPathExpr {

	public ASTPathExprList(int id) {
		super(id);
	}

	public ASTPathExprList(SyntaxTreeBuilder p, int id) {
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