/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public class ASTBound extends ASTBooleanExpr {

	public ASTBound() {
		this(SyntaxTreeBuilderTreeConstants.JJTBOUND);
	}

	public ASTBound(ASTValueExpr operand) {
		this();
		setOperand(operand);
	}

	public ASTBound(int id) {
		super(id);
	}

	public ASTBound(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public ASTVar getOperand() {
		return (ASTVar)children.get(0);
	}

	public void setOperand(ASTValueExpr operand) {
		jjtAddChild(operand, 0);
	}
}
