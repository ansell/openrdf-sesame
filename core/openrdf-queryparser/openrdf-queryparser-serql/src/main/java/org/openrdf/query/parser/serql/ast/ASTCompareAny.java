/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public class ASTCompareAny extends ASTBooleanExpr {

	public ASTCompareAny(int id) {
		super(id);
	}

	public ASTCompareAny(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public ASTValueExpr getLeftOperand() {
		return (ASTValueExpr)children.get(0);
	}

	public ASTCompOperator getOperator() {
		return (ASTCompOperator)children.get(1);
	}

	public ASTTupleQuery getRightOperand() {
		return (ASTTupleQuery)children.get(2);
	}
}
