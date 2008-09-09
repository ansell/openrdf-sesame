/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.helpers;

import java.util.LinkedHashSet;
import java.util.Set;

import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.Var;

/**
 * A QueryModelVisitor that collects the names of (non-constant) variables that
 * are used in a query model.
 */
public class VarNameCollector extends QueryModelVisitorBase<RuntimeException> {

	public static Set<String> process(QueryModelNode node) {
		VarNameCollector collector = new VarNameCollector();
		node.visit(collector);
		return collector.getVarNames();
	}

	private Set<String> varNames = new LinkedHashSet<String>();

	public Set<String> getVarNames() {
		return varNames;
	}

	@Override
	public void meet(Var var) {
		if (!var.hasValue()) {
			varNames.add(var.getName());
		}
	}
}
