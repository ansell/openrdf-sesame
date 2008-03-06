/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.serql.ast;

import java.util.List;

import org.openrdf.util.collections.CastingList;

public class ASTQueryContainer extends SimpleNode {

	public ASTQueryContainer(int id) {
		super(id);
	}

	public ASTQueryContainer(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. */
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
		if (hasNamespaceDeclList()) {
			return new CastingList<ASTNamespaceDecl>(children.subList(1, children.size()));
		}

		return null;
	}
}
