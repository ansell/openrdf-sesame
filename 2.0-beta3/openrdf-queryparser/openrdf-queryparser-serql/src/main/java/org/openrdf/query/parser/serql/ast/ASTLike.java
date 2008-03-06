/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public class ASTLike extends ASTBooleanExpr {

	private boolean _ignoreCase = false;

	public ASTLike(int id) {
		super(id);
	}

	public ASTLike(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. */
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public boolean ignoreCase() {
		return _ignoreCase;
	}

	public void setIgnoreCase(boolean ignoreCase) {
		_ignoreCase = ignoreCase;
	}

	public ASTValueExpr getValueExpr() {
		return (ASTValueExpr)children.get(0);
	}

	public ASTString getPattern() {
		return (ASTString)children.get(1);
	}

	public String toString() {
		String result = super.toString();

		if (_ignoreCase) {
			result += " (ignore case)";
		}

		return result;
	}
}
