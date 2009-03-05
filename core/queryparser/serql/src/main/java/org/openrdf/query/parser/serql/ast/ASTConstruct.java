/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

import java.util.List;

import info.aduna.collections.CastingList;

public class ASTConstruct extends SimpleNode {

	private boolean distinct = false;

	private boolean wildcard = false;

	public ASTConstruct(int id) {
		super(id);
	}

	public ASTConstruct(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	public boolean isDistinct() {
		return distinct;
	}

	public boolean isWildcard() {
		return wildcard;
	}

	public void setWildcard(boolean wildcard) {
		this.wildcard = wildcard;
	}

	public List<ASTPathExpr> getPathExprList() {
		return new CastingList<ASTPathExpr>(children);
	}

	@Override
	public String toString() {
		String result = super.toString();

		if (distinct) {
			result += " (distinct)";
		}

		if (wildcard) {
			result += " (*)";
		}

		return result;
	}
}
