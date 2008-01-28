/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public abstract class ASTQuery extends SimpleNode {

	public ASTQuery(int id) {
		super(id);
	}

	public ASTQuery(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}
}
