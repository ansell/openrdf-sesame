/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.serql.ast;

public class ASTNodeElem extends SimpleNode {

	public ASTNodeElem(int id) {
		super(id);
	}

	public ASTNodeElem(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. */
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}
	
	/**
	 * Gets the node element's child node.
	 *
	 * @return A variable, value or reified statement.
	 */
	public Node getChild() {
		return children.get(0);
	}
}
