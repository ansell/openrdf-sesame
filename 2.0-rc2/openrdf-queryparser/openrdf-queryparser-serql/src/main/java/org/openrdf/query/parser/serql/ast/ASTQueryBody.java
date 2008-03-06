/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

import java.util.ArrayList;
import java.util.List;

public class ASTQueryBody extends SimpleNode {

	public ASTQueryBody(int id) {
		super(id);
	}

	public ASTQueryBody(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public List<ASTFrom> getFromClauseList() {
		List<ASTFrom> fromClauseList = new ArrayList<ASTFrom>(children.size());

		for (Node n : children) {
			if (n instanceof ASTFrom) {
				fromClauseList.add((ASTFrom)n);
			}
			else {
				break;
			}
		}

		return fromClauseList;
	}

	public boolean hasWhereClause() {
		return getWhereClause() != null;
	}

	public ASTWhere getWhereClause() {
		for (Node n : children) {
			if (n instanceof ASTWhere) {
				return (ASTWhere)n;
			}
		}

		return null;
	}

	public boolean hasLimitClause() {
		return getLimitClause() != null;
	}

	public ASTLimit getLimitClause() {
		for (Node n : children) {
			if (n instanceof ASTLimit) {
				return (ASTLimit)n;
			}
		}

		return null;
	}

	public boolean hasOffsetClause() {
		return getOffsetClause() != null;
	}

	public ASTOffset getOffsetClause() {
		for (Node n : children) {
			if (n instanceof ASTOffset) {
				return (ASTOffset)n;
			}
		}

		return null;
	}
}
