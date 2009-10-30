/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public class ASTConstructQuery extends ASTGraphQuery {

	public ASTConstructQuery(int id) {
		super(id);
	}

	public ASTConstructQuery(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public ASTConstruct getConstructClause() {
		return (ASTConstruct)children.get(0);
	}

	public boolean hasQueryBody() {
		return getQueryBody() != null;
	}

	public ASTQueryBody getQueryBody() {
		return jjtGetChild(ASTQueryBody.class);
	}

	public boolean hasOrderBy() {
		return getOrderBy() != null;
	}

	public ASTOrderBy getOrderBy() {
		return jjtGetChild(ASTOrderBy.class);
	}

	public boolean hasLimit() {
		return getLimit() != null;
	}

	public ASTLimit getLimit() {
		return jjtGetChild(ASTLimit.class);
	}

	public boolean hasOffset() {
		return getOffset() != null;
	}

	public ASTOffset getOffset() {
		return jjtGetChild(ASTOffset.class);
	}
}
