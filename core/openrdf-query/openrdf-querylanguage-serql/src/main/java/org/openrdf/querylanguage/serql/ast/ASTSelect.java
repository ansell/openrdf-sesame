/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.serql.ast;

import java.util.List;

import org.openrdf.util.collections.CastingList;

public class ASTSelect extends SimpleNode {

	private boolean _distinct = false;

	private boolean _wildcard = false;

	public ASTSelect(int id) {
		super(id);
	}

	public ASTSelect(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. */
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public void setDistinct(boolean distinct) {
		_distinct = distinct;
	}

	public boolean isDistinct() {
		return _distinct;
	}

	public boolean isWildcard() {
		return _wildcard;
	}

	public void setWildcard(boolean wildcard) {
		_wildcard = wildcard;
	}
	
	public List<ASTProjectionElem> getProjectionElemList() {
		return new CastingList<ASTProjectionElem>(children);
	}

	public String toString() {
		String result = super.toString();

		if (_distinct) {
			result += " (distinct)";
		}

		if (_wildcard) {
			result += " (*)";
		}

		return result;
	}
}
