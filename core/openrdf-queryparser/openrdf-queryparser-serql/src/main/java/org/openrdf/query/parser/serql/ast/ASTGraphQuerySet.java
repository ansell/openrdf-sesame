/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public abstract class ASTGraphQuerySet extends ASTGraphQuery {

	public ASTGraphQuerySet(int i) {
		super(i);
	}

	public ASTGraphQuerySet(SyntaxTreeBuilder p, int i) {
		super(p, i);
	}

	public ASTGraphQuery getLeftArg() {
		return (ASTGraphQuery)children.get(0);
	}

	public ASTGraphQuery getRightArg() {
		return (ASTGraphQuery)children.get(1);
	}
}
