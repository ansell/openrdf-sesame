/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public class ASTGraphUnion extends ASTGraphQuerySet {

	private boolean _distinct = true;

	public ASTGraphUnion(int id) {
		super(id);
	}

	public ASTGraphUnion(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. */
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public boolean isDistinct() {
		return _distinct;
	}

	public void setDistinct(boolean distinct) {
		_distinct = distinct;
	}

	public String toString() {
		String result = super.toString();

		if (_distinct) {
			result += " (distinct)";
		}

		return result;
	}
}
