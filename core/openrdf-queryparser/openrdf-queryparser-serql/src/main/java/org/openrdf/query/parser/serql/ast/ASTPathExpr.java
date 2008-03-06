/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public abstract class ASTPathExpr extends SimpleNode {

	public ASTPathExpr(int id) {
		super(id);
	}

	public ASTPathExpr(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}
}
