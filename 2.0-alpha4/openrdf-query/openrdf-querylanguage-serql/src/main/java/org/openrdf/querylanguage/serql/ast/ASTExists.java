/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.serql.ast;

public class ASTExists extends ASTBooleanExpr {

	public ASTExists(int id) {
		super(id);
	}

	public ASTExists(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. */
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}
	
	public ASTTupleQuery getOperand() {
		return (ASTTupleQuery)children.get(0);
	}
}
