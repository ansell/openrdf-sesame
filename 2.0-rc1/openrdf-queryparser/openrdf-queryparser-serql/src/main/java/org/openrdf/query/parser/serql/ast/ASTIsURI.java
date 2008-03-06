/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public class ASTIsURI extends ASTBooleanExpr {

	public ASTIsURI(int id) {
		super(id);
	}

	public ASTIsURI(SyntaxTreeBuilder p, int id) {
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
