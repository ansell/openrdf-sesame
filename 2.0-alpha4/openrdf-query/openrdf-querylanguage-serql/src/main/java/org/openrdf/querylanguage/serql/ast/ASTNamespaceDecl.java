/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.serql.ast;

public class ASTNamespaceDecl extends SimpleNode {

	private String _prefix;

	public ASTNamespaceDecl(int id) {
		super(id);
	}

	public ASTNamespaceDecl(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. */
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public String getPrefix() {
		return _prefix;
	}

	public void setPrefix(String prefix) {
		_prefix = prefix;
	}
	
	public ASTURI getURI() {
		return (ASTURI)jjtGetChild(0);
	}

	public String toString() {
		return super.toString() + " (\"" + _prefix + "\")";
	}
}
