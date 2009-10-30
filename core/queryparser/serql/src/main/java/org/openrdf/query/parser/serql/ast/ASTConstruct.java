/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;


public class ASTConstruct extends SimpleNode {

	private boolean distinct = false;

	private boolean reduced = false;

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

	public void setReduced(boolean reduced) {
		this.reduced = reduced;
	}

	public boolean isReduced() {
		return reduced;
	}

	public boolean isWildcard() {
		return wildcard;
	}

	public void setWildcard(boolean wildcard) {
		this.wildcard = wildcard;
	}

	public ASTPathExpr getPathExpr() {
		return (ASTPathExpr)children.get(0);
	}

	@Override
	public String toString() {
		String result = super.toString();

		if (distinct || reduced || wildcard) {
			result += " (";
			if (distinct) {
				result += " distinct";
			}
			if (reduced) {
				result += " reduced";
			}
			if (wildcard) {
				result += " *";
			}
			result += " )";
		}

		return result;
	}
}
