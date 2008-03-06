/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.serql.ast;

import java.util.List;

import org.openrdf.util.collections.CastingList;

public class ASTNode extends SimpleNode {

	public ASTNode(int id) {
		super(id);
	}

	public ASTNode(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. */
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}
	
	public List<ASTNodeElem> getNodeElemList() {
		return new CastingList<ASTNodeElem>(children);
	}
}
