/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.evaluation;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStatistics;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.sail.helpers.SailUtil;
import org.openrdf.store.StoreException;

/**
 * Calculate the statistics based on the sum of the size from each member.
 * 
 * @author James Leigh
 */
public class FederationStatistics extends EvaluationStatistics {

	final Logger logger = LoggerFactory.getLogger(FederationStatistics.class);

	PatternCalculator calculator;

	public FederationStatistics(Collection<? extends RepositoryConnection> members, QueryModel query) {
		this.calculator = new PatternCalculator(members, query);
	}

	public double getCardinality(TupleExpr expr) {
		Calculator cc = new Calculator();
		expr.visit(cc);
		return cc.getCardinality();
	}

	public void await()
		throws StoreException
	{
		calculator.await();
	}

	protected class Calculator extends CardinalityCalculator {

		@Override
		protected double getCardinality(StatementPattern sp) {
			try {
				long cardinality = calculator.getCardinality(sp);
				double result = cardinality;
				if (result == Double.NaN || result < 0)
					return Double.MAX_VALUE;
				return result;
			}
			catch (StoreException e) {
				logger.error(e.toString(), e);
				return super.getCardinality();
			}
		}
	}

	static class PatternCounter extends QueryModelVisitorBase<RuntimeException> {

		private int count;

		private QueryModel query;

		public PatternCounter(QueryModel query) {
			this.query = query;
		}

		public int count() {
			count = 0;
			query.visit(this);
			return count;
		}

		@Override
		public void meet(StatementPattern sp) {
			count++;
		}
	}

	static class PatternCalculator extends QueryModelVisitorBase<RuntimeException> {

		static Executor executor = Executors.newCachedThreadPool();

		CountDownLatch latch;

		volatile Exception exception;

		private Collection<? extends RepositoryConnection> members;

		private Map<List<Value>, AtomicLong> cardinalities = new ConcurrentHashMap<List<Value>, AtomicLong>();

		public PatternCalculator(Collection<? extends RepositoryConnection> members, QueryModel query) {
			this.members = members;
			int count = new PatternCounter(query).count();
			this.latch = new CountDownLatch(count * members.size());
			query.visit(this);
		}

		public long getCardinality(StatementPattern sp)
			throws StoreException
		{
			await();
			Resource subj = (Resource)getConstantValue(sp.getSubjectVar());
			URI pred = (URI)getConstantValue(sp.getPredicateVar());
			Value obj = getConstantValue(sp.getObjectVar());
			Resource context = (Resource)getConstantValue(sp.getContextVar());
			List<Value> key = Arrays.asList(subj, pred, obj, context);
			return cardinalities.get(key).longValue();
		}

		@Override
		public void meet(StatementPattern sp) {
			final Resource subj = (Resource)getConstantValue(sp.getSubjectVar());
			final URI pred = (URI)getConstantValue(sp.getPredicateVar());
			final Value obj = getConstantValue(sp.getObjectVar());
			final Resource context = (Resource)getConstantValue(sp.getContextVar());
			List<Value> key = Arrays.asList(subj, pred, obj, context);
			if (cardinalities.containsKey(key)) {
				for (int i = 0, n = members.size(); i < n; i++) {
					latch.countDown(); // run down latch
				}
			}
			else {
				final AtomicLong cardinality = new AtomicLong(0l);
				cardinalities.put(key, cardinality);
				for (final RepositoryConnection member : members) {
					final StoreException source = SailUtil.isDebugEnabled() ? new StoreException() : null;
					executor.execute(new Runnable() {

						public void run() {
							try {
								long size = member.sizeMatch(subj, pred, obj, true, context);
								if (size > 0) {
									cardinality.getAndAdd(size);
								}
							}
							catch (RuntimeException e) {
								if (source != null) {
									source.initCause(e);
									exception = source;
								}
								else {
									exception = e;
								}
							}
							catch (StoreException e) {
								if (source != null) {
									source.initCause(e);
									exception = source;
								}
								else {
									exception = e;
								}
							}
							finally {
								latch.countDown();
							}
						}
					});
				}
			}
		}

		public void await()
			throws StoreException
		{
			try {
				latch.await();
				if (exception != null)
					throw exception;
			}
			catch (StoreException e) {
				throw e;
			}
			catch (RuntimeException e) {
				throw e;
			}
			catch (Exception e) {
				throw new StoreException(e);
			}
		}

		private Value getConstantValue(Var var) {
			return (var != null) ? var.getValue() : null;
		}
	}

}
