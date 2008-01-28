/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

import java.util.List;

import info.aduna.collections.CastingList;


public class ASTAnd extends ASTBooleanExpr {

	public ASTAnd(int id) {
		super(id);
	}

	public ASTAnd(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}
	
	public List<ASTBooleanExpr> getOperandList() {
		return new CastingList<ASTBooleanExpr>(children);
	}
}
