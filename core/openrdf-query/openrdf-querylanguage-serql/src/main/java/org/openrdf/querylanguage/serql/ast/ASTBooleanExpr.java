/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.serql.ast;

public abstract class ASTBooleanExpr extends SimpleNode {

	public ASTBooleanExpr(int i) {
		super(i);
	}

	public ASTBooleanExpr(SyntaxTreeBuilder p, int i) {
		super(p, i);
	}
}
