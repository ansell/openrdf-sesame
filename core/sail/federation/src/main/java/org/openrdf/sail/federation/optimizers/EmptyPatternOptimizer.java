/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.optimizers;

import java.util.Collection;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.store.StoreException;

/**
 * Remove StatementPatterns that have no statements.
 * 
 * @author James Leigh
 */
public class EmptyPatternOptimizer extends QueryModelVisitorBase<StoreException> implements QueryOptimizer {

	private Collection<RepositoryConnection> members;

	public EmptyPatternOptimizer(Collection<RepositoryConnection> members) {
		this.members = members;
	}

	public void optimize(QueryModel query, BindingSet bindings)
		throws StoreException
	{
		query.visit(this);
	}

	@Override
	public void meet(StatementPattern node)
		throws StoreException
	{
		Resource subj = (Resource)node.getSubjectVar().getValue();
		URI pred = (URI)node.getPredicateVar().getValue();
		Value obj = node.getObjectVar().getValue();
		Resource[] ctx = getContexts(node.getContextVar());
		for (RepositoryConnection member : members) {
			if (member.hasMatch(subj, pred, obj, true, ctx)) {
				return;
			}
		}
		node.replaceWith(new EmptySet());
	}

	private Resource[] getContexts(Var var) {
		if (var == null || !var.hasValue())
			return new Resource[0];
		return new Resource[] { (Resource)var.getValue() };
	}

}
