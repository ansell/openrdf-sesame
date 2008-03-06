/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.serql.ast;

import org.openrdf.querymodel.Compare;

public class ASTCompOperator extends SimpleNode {

	private Compare.Operator _value;

	public ASTCompOperator(int id) {
		super(id);
	}

	public ASTCompOperator(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. */
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public Compare.Operator getValue() {
		return _value;
	}

	public void setValue(Compare.Operator operator) {
		_value = operator;
	}
}
