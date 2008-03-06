/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.serql.ast;

public class ASTURI extends ASTValue {

	private String _value;

	public ASTURI(int id) {
		super(id);
	}

	public ASTURI(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	public ASTURI(int id, String uri) {
		this(id);
		setValue(uri);
	}

	/** Accept the visitor. * */
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public String getValue() {
		return _value;
	}

	public void setValue(String value) {
		_value = value;
	}

	public String toString() {
		return super.toString() + " (" + _value + ")";
	}
}
