/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */

package org.openrdf.query.parser.serql.ast;

public class ASTBooleanConstant extends ASTBooleanExpr {

	private boolean value;

	public ASTBooleanConstant(int id) {
		super(id);
	}

	public ASTBooleanConstant(boolean value) {
		this(SyntaxTreeBuilderTreeConstants.JJTBOOLEANCONSTANT);
		setValue(value);
	}

	public ASTBooleanConstant(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public boolean getValue() {
		return value;
	}

	public void setValue(boolean value) {
		this.value = value;
	}
}
