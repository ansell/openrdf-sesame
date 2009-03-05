/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public class ASTQName extends ASTValue {

	private String value;

	public ASTQName(int id) {
		super(id);
	}

	public ASTQName(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	public ASTQName(int id, String value) {
		this(id);
		setValue(value);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return super.toString() + " (" + value + ")";
	}
}
