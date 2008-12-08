/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.evaluation;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStatistics;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.store.StoreException;

public class FederationStatistics extends EvaluationStatistics {
	final Logger logger = LoggerFactory.getLogger(FederationStatistics.class);

	private Collection<RepositoryConnection> members;

	public FederationStatistics(Collection<RepositoryConnection> members) {
		this.members = members;
	}

	public double getCardinality(TupleExpr expr) {
		Calculator cc = createCardinalityCalculator();
		expr.visit(cc);
		return cc.getCardinality();
	}

	@Override
	protected Calculator createCardinalityCalculator() {
		return new Calculator();
	}

	protected class Calculator extends CardinalityCalculator {
		@Override
		protected double getCardinality(StatementPattern sp) {
			Resource subj = (Resource)getConstantValue(sp.getSubjectVar());
			URI pred = (URI)getConstantValue(sp.getPredicateVar());
			Value obj = getConstantValue(sp.getObjectVar());
			Resource context = (Resource)getConstantValue(sp.getContextVar());
			try {
				long size = size(subj, pred, obj, true, context);
				double result = size;
				if (result == Double.NaN || result < 0)
					return Double.MAX_VALUE;
				return result;
			}
			catch (StoreException e) {
				logger.error(e.toString(), e);
				return super.getCardinality();
			}
		}

		private Value getConstantValue(Var var) {
			return (var != null) ? var.getValue() : null;
		}
	}

	long size(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		long size = 0;
		for (RepositoryConnection member : members) {
			size += member.size(subj, pred, obj, includeInferred, contexts);
		}
		return size;
	}

}
