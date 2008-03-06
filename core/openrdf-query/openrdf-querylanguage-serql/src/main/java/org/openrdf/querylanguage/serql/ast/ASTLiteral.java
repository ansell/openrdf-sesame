/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.serql.ast;

public class ASTLiteral extends ASTValue {

	private String _label;

	private String _lang;

	public ASTLiteral(int id) {
		super(id);
	}

	public ASTLiteral(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. */
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public String getLabel() {
		return _label;
	}

	public void setLabel(String label) {
		_label = label;
	}

	public String getLang() {
		return _lang;
	}

	public boolean hasLang() {
		return _lang != null;
	}

	public void setLang(String lang) {
		_lang = lang;
	}

	public ASTValueExpr getDatatypeNode() {
		if (children.size() >= 1) {
			return (ASTValueExpr)children.get(0);
		}

		return null;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(super.toString());

		sb.append(" (\"").append(_label).append("\"");

		if (_lang != null) {
			sb.append('@').append(_lang);
		}

		sb.append(")");

		return sb.toString();
	}
}
