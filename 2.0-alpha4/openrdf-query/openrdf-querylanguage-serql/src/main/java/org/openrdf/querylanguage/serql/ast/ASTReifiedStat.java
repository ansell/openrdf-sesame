/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.serql.ast;

public class ASTReifiedStat extends SimpleNode {

	private ASTVar _id;

	public ASTReifiedStat(int id) {
		super(id);
	}

	public ASTReifiedStat(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. */
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public ASTVar getID() {
		return _id;
	}

	public void setID(ASTVar id) {
		_id = id;
	}
	
	public ASTNodeElem getSubject() {
		return (ASTNodeElem)children.get(0);
	}
	
	public ASTEdge getPredicate() {
		return (ASTEdge)children.get(1);
	}
	
	public ASTNodeElem getObject() {
		return (ASTNodeElem)children.get(2);
	}
}
