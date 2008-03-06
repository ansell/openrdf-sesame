/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public class ASTVar extends ASTValueExpr {

	private String _name;

	private boolean _anonymous = false;

	public ASTVar(int id) {
		super(id);
	}

	public ASTVar(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. */
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public boolean isAnonymous() {
		return _anonymous;
	}

	public void setAnonymous(boolean anonymous) {
		_anonymous = anonymous;
	}

	public String toString() {
		String result = super.toString() + " (" + _name + ")";

		if (_anonymous) {
			result += " (anonymous)";
		}

		return result;
	}
}
