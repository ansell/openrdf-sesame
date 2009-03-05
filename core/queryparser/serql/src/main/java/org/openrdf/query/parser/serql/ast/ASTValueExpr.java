/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public abstract class ASTValueExpr extends SimpleNode {

	public ASTValueExpr(int i) {
		super(i);
	}

	public ASTValueExpr(SyntaxTreeBuilder p, int i) {
		super(p, i);
	}
}
