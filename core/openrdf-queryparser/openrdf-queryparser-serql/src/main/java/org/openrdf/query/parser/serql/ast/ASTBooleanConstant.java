/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */

package org.openrdf.query.parser.serql.ast;

public class ASTBooleanConstant extends ASTBooleanExpr {

	private boolean _value;

	public ASTBooleanConstant(int id) {
		super(id);
	}

	public ASTBooleanConstant(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. */
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public boolean getValue() {
		return _value;
	}

	public void setValue(boolean value) {
		_value = value;
	}
}
