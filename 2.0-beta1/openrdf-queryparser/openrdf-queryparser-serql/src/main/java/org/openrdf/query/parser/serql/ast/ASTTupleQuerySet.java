/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public abstract class ASTTupleQuerySet extends ASTTupleQuery {

	public ASTTupleQuerySet(int i) {
		super(i);
	}

	public ASTTupleQuerySet(SyntaxTreeBuilder p, int i) {
		super(p, i);
	}

	public ASTTupleQuery getLeftArg() {
		return (ASTTupleQuery)children.get(0);
	}

	public ASTTupleQuery getRightArg() {
		return (ASTTupleQuery)children.get(1);
	}
}
