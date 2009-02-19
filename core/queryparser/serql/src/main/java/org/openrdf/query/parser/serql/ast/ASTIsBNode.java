/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public class ASTIsBNode extends ASTBooleanExpr {

	public ASTIsBNode(int id) {
		super(id);
	}

	public ASTIsBNode(SyntaxTreeBuilder p, int id) {
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
}
