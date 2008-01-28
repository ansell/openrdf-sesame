/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

import java.util.List;

public class ASTQueryContainer extends SimpleNode {

	public ASTQueryContainer(int id) {
		super(id);
	}

	public ASTQueryContainer(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public ASTQuery getQuery() {
		return (ASTQuery)children.get(0);
	}

	public boolean hasNamespaceDeclList() {
		return children.size() >= 2;
	}

	public List<ASTNamespaceDecl> getNamespaceDeclList() {
		return super.jjtGetChildren(ASTNamespaceDecl.class);
	}
}
