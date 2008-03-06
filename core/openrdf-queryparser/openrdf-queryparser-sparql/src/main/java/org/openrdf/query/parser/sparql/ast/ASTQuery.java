/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql.ast;

import java.util.List;

public abstract class ASTQuery extends SimpleNode {

	public ASTQuery(int id) {
		super(id);
	}

	public ASTQuery(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	public List<ASTDatasetClause> getDatasetClauseList() {
		return jjtGetChildren(ASTDatasetClause.class);
	}

	public ASTWhereClause getWhereClause() {
		return jjtGetChild(ASTWhereClause.class);
	}

	public ASTOrderClause getOrderClause() {
		return jjtGetChild(ASTOrderClause.class);
	}

	public ASTLimit getLimit() {
		return jjtGetChild(ASTLimit.class);
	}

	public ASTOffset getOffset() {
		return jjtGetChild(ASTOffset.class);
	}
}
