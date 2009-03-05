/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public class ASTLike extends ASTBooleanExpr {

	private boolean ignoreCase = false;

	public ASTLike(int id) {
		super(id);
	}

	public ASTLike(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public boolean ignoreCase() {
		return ignoreCase;
	}

	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	public ASTValueExpr getValueExpr() {
		return (ASTValueExpr)children.get(0);
	}

	public ASTString getPattern() {
		return (ASTString)children.get(1);
	}

	@Override
	public String toString() {
		String result = super.toString();

		if (ignoreCase) {
			result += " (ignore case)";
		}

		return result;
	}
}
