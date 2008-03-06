/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.serql.ast;

public class ASTConstructQuery extends ASTGraphQuery {

	public ASTConstructQuery(int id) {
		super(id);
	}

	public ASTConstructQuery(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. */
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public ASTConstruct getConstructClause() {
		return (ASTConstruct)children.get(0);
	}

	public boolean hasQueryBody() {
		return children.size() >= 2;
	}

	public ASTQueryBody getQueryBody() {
		if (children.size() >= 2) {
			return (ASTQueryBody)children.get(1);
		}
		
		return null;
	}
}
