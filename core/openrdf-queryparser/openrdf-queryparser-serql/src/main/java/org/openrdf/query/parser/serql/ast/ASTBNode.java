/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public class ASTBNode extends ASTValue {

	private String _id;

	public ASTBNode(int id) {
		super(id);
	}

	public ASTBNode(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. */
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public String getID() {
		return _id;
	}

	public void setID(String id) {
		_id = id;
	}

	public String toString() {
		return super.toString() + " (_:" + _id + ")";
	}
}
