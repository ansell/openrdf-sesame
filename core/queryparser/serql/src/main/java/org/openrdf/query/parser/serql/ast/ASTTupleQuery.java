/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public abstract class ASTTupleQuery extends ASTQuery {

	public ASTTupleQuery(int i) {
		super(i);
	}

	public ASTTupleQuery(SyntaxTreeBuilder p, int i) {
		super(p, i);
	}
}
