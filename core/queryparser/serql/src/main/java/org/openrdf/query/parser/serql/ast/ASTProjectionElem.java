/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public class ASTProjectionElem extends SimpleNode {

	public ASTProjectionElem(int id) {
		super(id);
	}

	public ASTProjectionElem(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public ASTValueExpr getValueExpr() {
		return (ASTValueExpr)children.get(0);
	}

	public boolean hasAlias() {
		return getAlias() != null;
	}

	public String getAlias() {
		if (children.size() >= 2) {
			Node aliasNode = children.get(1);

			if (aliasNode instanceof ASTString) {
				return ((ASTString)aliasNode).getValue();
			}
			else if (aliasNode instanceof ASTVar) {
				return ((ASTVar)aliasNode).getName();
			}
		}

		return null;
	}
}
