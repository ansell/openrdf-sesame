/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.serql.ast;

public class ASTLocalName extends ASTValueExpr {

	public ASTLocalName(int id) {
		super(id);
	}

	public ASTLocalName(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. */
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public ASTVar getOperand() {
		return (ASTVar)children.get(0);
	}
}
