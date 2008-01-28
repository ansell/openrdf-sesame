/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public class ASTNot extends ASTBooleanExpr {

	public ASTNot() {
		this(SyntaxTreeBuilderTreeConstants.JJTNOT);
	}

	public ASTNot(ASTBooleanExpr operand) {
		this();
		setOperand(operand);
	}

	public ASTNot(int id) {
		super(id);
	}

	public ASTNot(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public ASTBooleanExpr getOperand() {
		return (ASTBooleanExpr)children.get(0);
	}

	public void setOperand(ASTBooleanExpr operand) {
		jjtAddChild(operand, 0);
	}
}
