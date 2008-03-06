/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.sparql;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.querymodel.Selection;
import org.openrdf.querymodel.StatementPattern;
import org.openrdf.querymodel.helpers.QueryModelVisitorBase;

/**
 * A QueryModelVisitor that collects StatementPattern's from a query model.
 */
class StatementPatternCollector extends QueryModelVisitorBase {

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
