/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.algebra.Selection;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * A QueryModelVisitor that collects StatementPattern's from a query model.
 */
class StatementPatternCollector extends QueryModelVisitorBase<RuntimeException> {

	private List<StatementPattern> stPatterns = new ArrayList<StatementPattern>();

	public List<StatementPattern> getStatementPatterns() {
		return stPatterns;
	}

	public void meet(Selection node) {
		// Skip boolean constraints
		node.getArg().visit(this);
	}

	public void meet(StatementPattern node) {
		stPatterns.add(node);
	}
}
