/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIteratorIteration;
import info.aduna.iteration.DistinctIteration;
import info.aduna.iteration.ExceptionConvertingIteration;
import info.aduna.iteration.UnionIteration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.impl.BindingAssigner;
import org.openrdf.query.algebra.evaluation.impl.CompareOptimizer;
import org.openrdf.query.algebra.evaluation.impl.ConjunctiveConstraintSplitter;
import org.openrdf.query.algebra.evaluation.impl.ConstantOptimizer;
import org.openrdf.query.algebra.evaluation.impl.DisjunctiveConstraintOptimizer;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.query.algebra.evaluation.impl.SameTermFilterOptimizer;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.federation.evaluation.FederationStrategy;
import org.openrdf.sail.federation.optimizers.EmptyPatternOptimizer;
import org.openrdf.sail.federation.optimizers.FederationJoinOptimizer;
import org.openrdf.sail.federation.optimizers.OwnedTupleExprPruner;
import org.openrdf.sail.federation.optimizers.PrepareOwnedTupleExpr;
import org.openrdf.sail.federation.optimizers.QueryModelPruner;
import org.openrdf.sail.federation.optimizers.QueryMultiJoinOptimizer;
import org.openrdf.sail.helpers.SailBase;
import org.openrdf.sail.helpers.SailConnectionBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unions the results from multiple {@link RepositoryConnection} into one
 * {@link SailConnection}.
 * 
 * @author James Leigh
 * @author Arjohn Kampman
 */
abstract class FederationConnection extends SailConnectionBase {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(FederationConnection.class);

	private final Federation federation;

	private final ValueFactory valueFactory;

	protected final List<RepositoryConnection> members;

	public FederationConnection(Federation federation,
			List<RepositoryConnection> members) {
		super(new SailBase() {

			public boolean isWritable() throws SailException {
				return false;
			}

			public ValueFactory getValueFactory() {
				return null;
			}

			@Override
			protected void shutDownInternal() throws SailException {
				// ignore
			}

			@Override
			protected SailConnection getConnectionInternal()
					throws SailException {
				return null;
			}

			@Override
			protected void connectionClosed(SailConnection connection) {
				// ignore
			}
		});
		this.federation = federation;

		valueFactory = ValueFactoryImpl.getInstance();

		this.members = new ArrayList<RepositoryConnection>(members.size());
		for (RepositoryConnection member : members) {
			this.members.add(member);
		}
	}

	public ValueFactory getValueFactory() {
		return valueFactory;
	}

	@Override
	public void closeInternal() throws SailException {
		excute(new Procedure() {

			public void run(RepositoryConnection con)
					throws RepositoryException {
				con.close();
			}
		});
	}

	@Override
	public CloseableIteration<? extends Resource, SailException> getContextIDsInternal()
			throws SailException {
		CloseableIteration<? extends Resource, SailException> cursor = union(new Function<Resource>() {

			public CloseableIteration<? extends Resource, RepositoryException> call(
					RepositoryConnection member) throws RepositoryException {
				return member.getContextIDs();
			}
		});

		cursor = new DistinctIteration<Resource, SailException>(cursor);

		return cursor;
	}

	@Override
	public String getNamespaceInternal(String prefix) throws SailException {
		try {
			String namespace = null;
			for (RepositoryConnection member : members) {
				String candidate = member.getNamespace(prefix);
				if (namespace == null) {
					namespace = candidate;
				} else if (candidate != null && !candidate.equals(namespace)) {
					namespace = null; // NOPMD
					break;
				}
			}
			return namespace;
		} catch (RepositoryException e) {
			throw new SailException(e);
		}
	}

	@Override
	public CloseableIteration<? extends Namespace, SailException> getNamespacesInternal()
			throws SailException {
		Map<String, Namespace> namespaces = new HashMap<String, Namespace>();
		Set<String> prefixes = new HashSet<String>();

		try {
			for (RepositoryConnection member : members) {
				RepositoryResult<Namespace> memberNamespaces = member
						.getNamespaces();
				try {
					while (memberNamespaces.hasNext()) {
						Namespace next = memberNamespaces.next();
						String prefix = next.getPrefix();

						if (prefixes.add(prefix)) {
							namespaces.put(prefix, next);
						} else if (!next.getName().equals(
								namespaces.get(prefix).getName())) {
							namespaces.remove(prefix);
						}
					}
				} finally {
					memberNamespaces.close();
				}
			}
		} catch (RepositoryException e) {
			throw new SailException(e);
		}

		return new CloseableIteratorIteration<Namespace, SailException>(
				namespaces.values().iterator());
	}

	@Override
	public long sizeInternal(Resource... contexts) throws SailException {
		try {
			if (federation.isDistinct()) {
				long size = 0;
				for (RepositoryConnection member : members) {
					size += member.size(contexts);
				}
				return size; // NOPMD
			} else {
				CloseableIteration<? extends Statement, SailException> cursor = getStatements(
						null, null, null, true, contexts);
				try {
					long size = 0;
					while (cursor.hasNext()) {
						cursor.next();
						size++;
					}
					return size;
				} finally {
					cursor.close();
				}
			}
		} catch (RepositoryException e) {
			throw new SailException(e);
		}
	}

	@Override
	public CloseableIteration<? extends Statement, SailException> getStatementsInternal(
			final Resource subj, final URI pred, final Value obj,
			final boolean includeInferred, final Resource... contexts)
			throws SailException {
		CloseableIteration<? extends Statement, SailException> cursor = union(new Function<Statement>() {

			public CloseableIteration<? extends Statement, RepositoryException> call(
					RepositoryConnection member) throws RepositoryException {
				return member.getStatements(subj, pred, obj, includeInferred,
						contexts);
			}
		});

		if (!federation.isDistinct() && !isLocal(pred)) {
			// Filter any duplicates
			cursor = new DistinctIteration<Statement, SailException>(cursor);
		}

		return cursor;
	}

	@Override
	public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluateInternal(
			TupleExpr query, Dataset dataset, BindingSet bindings, boolean inf)
			throws SailException {
		TripleSource tripleSource = new FederationTripleSource(inf);
		EvaluationStrategyImpl strategy = new FederationStrategy(federation,
				tripleSource, dataset);
		TupleExpr qry = optimize(query, dataset, bindings, strategy);
		try {
			return strategy.evaluate(qry, EmptyBindingSet.getInstance());
		} catch (QueryEvaluationException e) {
			throw new SailException(e);
		}
	}

	private class FederationTripleSource implements TripleSource {

		private final boolean inf;

		public FederationTripleSource(boolean includeInferred) {
			this.inf = includeInferred;
		}

		public CloseableIteration<? extends Statement, QueryEvaluationException> getStatements(
				Resource subj, URI pred, Value obj, Resource... contexts)
				throws QueryEvaluationException {
			try {
				CloseableIteration<? extends Statement, SailException> result = FederationConnection.this
						.getStatements(subj, pred, obj, inf, contexts);
				return new ExceptionConvertingIteration<Statement, QueryEvaluationException>(
						result) {

					@Override
					protected QueryEvaluationException convert(Exception e) {
						return new QueryEvaluationException(e);
					}
				};
			} catch (SailException e) {
				throw new QueryEvaluationException(e);
			}
		}

		public ValueFactory getValueFactory() {
			return valueFactory;
		}
	}

	private TupleExpr optimize(TupleExpr parsed, Dataset dataset,
			BindingSet bindings, EvaluationStrategyImpl strategy)
			throws SailException {
		LOGGER.trace("Incoming query model:\n{}", parsed.toString());

		// Clone the tuple expression to allow for more aggressive optimisations
		TupleExpr query = new QueryRoot(parsed.clone());

		new BindingAssigner().optimize(query, dataset, bindings);
		new ConstantOptimizer(strategy).optimize(query, dataset, bindings);
		new CompareOptimizer().optimize(query, dataset, bindings);
		new ConjunctiveConstraintSplitter().optimize(query, dataset, bindings);
		new DisjunctiveConstraintOptimizer().optimize(query, dataset, bindings);
		new SameTermFilterOptimizer().optimize(query, dataset, bindings);
		new QueryModelPruner().optimize(query, dataset, bindings);

		new QueryMultiJoinOptimizer().optimize(query, dataset, bindings);
		// new FilterOptimizer().optimize(query, dataset, bindings);

		new EmptyPatternOptimizer(members).optimize(query, dataset, bindings);
		boolean distinct = federation.isDistinct();
		PrefixHashSet local = federation.getLocalPropertySpace();
		new FederationJoinOptimizer(members, distinct, local).optimize(query,
				dataset, bindings);
		new OwnedTupleExprPruner().optimize(query, dataset, bindings);
		new QueryModelPruner().optimize(query, dataset, bindings);
		new QueryMultiJoinOptimizer().optimize(query, dataset, bindings);

		new PrepareOwnedTupleExpr().optimize(query, dataset, bindings);

		LOGGER.trace("Optimized query model:\n{}", query.toString());
		return query;
	}

	interface Procedure {
		void run(RepositoryConnection member) throws RepositoryException;
	}

	void excute(Procedure operation) throws SailException { // NOPMD
		RepositoryException storeExc = null;
		RuntimeException runtimeExc = null;

		for (RepositoryConnection member : members) {
			try {
				operation.run(member);
			} catch (RepositoryException e) {
				LOGGER.error(
						"Failed to execute procedure on federation members", e);
				if (storeExc == null) {
					storeExc = e;
				}
			} catch (RuntimeException e) {
				LOGGER.error(
						"Failed to execute procedure on federation members", e);
				if (runtimeExc == null) {
					runtimeExc = e;
				}
			}
		}

		if (storeExc != null) {
			throw new SailException(storeExc);
		}

		if (runtimeExc != null) {
			throw runtimeExc;
		}
	}

	private interface Function<E> {
		CloseableIteration<? extends E, RepositoryException> call(
				RepositoryConnection member) throws RepositoryException;
	}

	private <E> CloseableIteration<? extends E, SailException> union(
			Function<E> function) throws SailException {
		List<CloseableIteration<? extends E, RepositoryException>> cursors = new ArrayList<CloseableIteration<? extends E, RepositoryException>>(
				members.size());

		try {
			for (RepositoryConnection member : members) {
				cursors.add(function.call(member));
			}
			UnionIteration<E, RepositoryException> result = new UnionIteration<E, RepositoryException>(
					cursors);
			return new ExceptionConvertingIteration<E, SailException>(result) {

				@Override
				protected SailException convert(Exception e) {
					return new SailException(e);
				}
			};
		} catch (RepositoryException e) {
			closeAll(cursors);
			throw new SailException(e);
		} catch (RuntimeException e) {
			closeAll(cursors);
			throw e;
		}
	}

	private boolean isLocal(URI pred) {
		if (pred == null) {
			return false; // NOPMD
		}

		PrefixHashSet hash = federation.getLocalPropertySpace();
		if (hash == null) {
			return false; // NOPMD
		}

		return hash.match(pred.stringValue());
	}

	private void closeAll(
			Iterable<? extends CloseableIteration<?, RepositoryException>> cursors) {
		for (CloseableIteration<?, RepositoryException> cursor : cursors) {
			try {
				cursor.close();
			} catch (RepositoryException e) {
				LOGGER.error("Failed to close cursor", e);
			}
		}
	}
}
