/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public class ASTNamespaceDecl extends SimpleNode {

	private String prefix;

	public ASTNamespaceDecl(int id) {
		super(id);
	}

	public ASTNamespaceDecl(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public ASTURI getURI() {
		return (ASTURI)jjtGetChild(0);
	}

	@Override
	public String toString() {
		return super.toString() + " (\"" + prefix + "\")";
	}
}
