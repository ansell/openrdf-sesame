/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public class ASTSelectQuery extends ASTTupleQuery {

	public ASTSelectQuery(int id) {
		super(id);
	}

	public ASTSelectQuery(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. */
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public ASTSelect getSelectClause() {
		return (ASTSelect)children.get(0);
	}

	public boolean hasQueryBody() {
		return children.size() >= 2;
	}

	public ASTQueryBody getQueryBody() {
		return (ASTQueryBody)children.get(1);
	}
}
