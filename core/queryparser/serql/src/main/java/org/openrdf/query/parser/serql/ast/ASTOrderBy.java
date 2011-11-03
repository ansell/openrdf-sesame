/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

import java.util.List;

public class ASTOrderBy extends SimpleNode {

	public ASTOrderBy(int id) {
		super(id);
	}

	public ASTOrderBy(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public List<ASTOrderExpr> getOrderExprList() {
		return new CastingList<ASTOrderExpr>(children);
	}
}