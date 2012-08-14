/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.helpers;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * A QueryModelVisitor that collects StatementPattern's from a query model.
 * StatementPatterns thet are part of filters/constraints are not included in
 * the result.
 */
public class StatementPatternCollector extends QueryModelVisitorBase<RuntimeException> {

	public static List<StatementPattern> process(QueryModelNode node) {
		StatementPatternCollector collector = new StatementPatternCollector();
		node.visit(collector);
		return collector.getStatementPatterns();
	}

	private List<StatementPattern> stPatterns = new ArrayList<StatementPattern>();

	public List<StatementPattern> getStatementPatterns() {
		return stPatterns;
	}

	@Override
	public void meet(Filter node)
	{
		// Skip boolean constraints
		node.getArg().visit(this);
	}

	@Override
	public void meet(StatementPattern node)
	{
		stPatterns.add(node);
	}
}
