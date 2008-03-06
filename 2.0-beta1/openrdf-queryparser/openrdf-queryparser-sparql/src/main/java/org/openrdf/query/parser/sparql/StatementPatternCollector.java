/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.algebra.Selection;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * A QueryModelVisitor that collects StatementPattern's from a query model.
 */
class StatementPatternCollector extends QueryModelVisitorBase<RuntimeException> {

	private List<StatementPattern> _stPatterns = new ArrayList<StatementPattern>();

	public List<StatementPattern> getStatementPatterns() {
		return _stPatterns;
	}

	public void meet(Selection node) {
		// Skip boolean constraints
		node.getArg().visit(this);
	}

	public void meet(StatementPattern node) {
		_stPatterns.add(node);
	}
}
