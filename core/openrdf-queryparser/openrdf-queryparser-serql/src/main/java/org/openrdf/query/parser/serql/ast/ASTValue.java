/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

/**
 * Abstract super type of all basic values (URIs, QNames, BNodes and Literals).
 */
public abstract class ASTValue extends ASTValueExpr {

	public ASTValue(int i) {
		super(i);
	}

	public ASTValue(SyntaxTreeBuilder p, int i) {
		super(p, i);
	}
}
