/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.sail.derived;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.IsolationLevel;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.federation.FederatedServiceResolver;
import org.openrdf.query.algebra.evaluation.federation.FederatedServiceResolverClient;
import org.openrdf.query.algebra.evaluation.impl.BindingAssigner;
import org.openrdf.query.algebra.evaluation.impl.CompareOptimizer;
import org.openrdf.query.algebra.evaluation.impl.ConjunctiveConstraintSplitter;
import org.openrdf.query.algebra.evaluation.impl.ConstantOptimizer;
import org.openrdf.query.algebra.evaluation.impl.DisjunctiveConstraintOptimizer;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStatistics;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.query.algebra.evaluation.impl.FilterOptimizer;
import org.openrdf.query.algebra.evaluation.impl.IterativeEvaluationOptimizer;
import org.openrdf.query.algebra.evaluation.impl.OrderLimitOptimizer;
import org.openrdf.query.algebra.evaluation.impl.QueryJoinOptimizer;
import org.openrdf.query.algebra.evaluation.impl.QueryModelNormalizer;
import org.openrdf.query.algebra.evaluation.impl.SameTermFilterOptimizer;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.sail.SailException;
import org.openrdf.sail.UnknownSailTransactionStateException;
import org.openrdf.sail.helpers.NotifyingSailConnectionBase;
import org.openrdf.sail.helpers.SailBase;
import org.openrdf.sail.inferencer.InferencerConnection;

/**
 * @author James Leigh
 */
public class DerivedSailConnection extends NotifyingSailConnectionBase implements InferencerConnection,
		FederatedServiceResolverClient
{

	/*-----------*
	 * Variables *
	 *-----------*/

	private final ValueFactory vf;

	private final EvaluationStatistics stats;

	private final RdfSource derivedFromExplicit;

	private final RdfSource derivedFromInferred;

	private final IsolationLevel defaultIsolationLevel;

	private RdfSource explicitOnlyDatasource;

	private RdfSource inferredOnlyDatasource;

	private RdfSource includeInferredDatasource;

	/**
	 * Connection specific resolver.
	 */
	private FederatedServiceResolver federatedServiceResolver;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * 
	 * @param sail
	 * @param explicit source of explicit-only statements
	 * @param inferred source of sinks inferred, but observes both
	 * @param stats
	 * @param resolver
	 */
	protected DerivedSailConnection(SailBase sail, RdfStore store, FederatedServiceResolver resolver)
	{
		super(sail);
		this.vf = sail.getValueFactory();
		this.derivedFromExplicit = store.getExplicitRdfSource();
		this.derivedFromInferred = store.getInferredRdfSource();
		this.defaultIsolationLevel = sail.getDefaultIsolationLevel();
		this.stats = store.getEvaluationStatistics();
		this.federatedServiceResolver = resolver;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public FederatedServiceResolver getFederatedServiceResolver() {
		return federatedServiceResolver;
	}

	public void setFederatedServiceResolver(FederatedServiceResolver resolver) {
		this.federatedServiceResolver = resolver;
	}

	protected EvaluationStrategy getEvaluationStrategy(Dataset dataset, TripleSource tripleSource) {
		return new EvaluationStrategyImpl(tripleSource, dataset, getFederatedServiceResolver());
	}

	@Override
	protected CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluateInternal(
			TupleExpr tupleExpr, Dataset dataset, BindingSet bindings, boolean includeInferred)
		throws SailException
	{
		logger.trace("Incoming query model:\n{}", tupleExpr);

		// Clone the tuple expression to allow for more aggresive optimizations
		tupleExpr = tupleExpr.clone();

		if (!(tupleExpr instanceof QueryRoot)) {
			// Add a dummy root node to the tuple expressions to allow the
			// optimizers to modify the actual root node
			tupleExpr = new QueryRoot(tupleExpr);
		}

		RdfSource source = source(includeInferred);
		RdfDataset rdfDataset = source.snapshot(getIsolationLevel());
		boolean releaseLock = true;

		try {

			TripleSource tripleSource = new DerivedTripleSource(vf, rdfDataset);
			EvaluationStrategy strategy = getEvaluationStrategy(dataset, tripleSource);

			new BindingAssigner().optimize(tupleExpr, dataset, bindings);
			new ConstantOptimizer(strategy).optimize(tupleExpr, dataset, bindings);
			new CompareOptimizer().optimize(tupleExpr, dataset, bindings);
			new ConjunctiveConstraintSplitter().optimize(tupleExpr, dataset, bindings);
			new DisjunctiveConstraintOptimizer().optimize(tupleExpr, dataset, bindings);
			new SameTermFilterOptimizer().optimize(tupleExpr, dataset, bindings);
			new QueryModelNormalizer().optimize(tupleExpr, dataset, bindings);
			new QueryJoinOptimizer(stats).optimize(tupleExpr, dataset, bindings);
			// new SubSelectJoinOptimizer().optimize(tupleExpr, dataset, bindings);
			new IterativeEvaluationOptimizer().optimize(tupleExpr, dataset, bindings);
			new FilterOptimizer().optimize(tupleExpr, dataset, bindings);
			new OrderLimitOptimizer().optimize(tupleExpr, dataset, bindings);

			logger.trace("Optimized query model:\n{}", tupleExpr);

			CloseableIteration<BindingSet, QueryEvaluationException> iter;
			iter = strategy.evaluate(tupleExpr, EmptyBindingSet.getInstance());
			iter = interlock(iter, rdfDataset, source);
			releaseLock = false;
			return iter;
		}
		catch (QueryEvaluationException e) {
			throw new SailException(e);
		}
		finally {
			if (releaseLock) {
				rdfDataset.close();
				source.close();
			}
		}
	}

	@Override
	protected void closeInternal()
		throws SailException
	{
		derivedFromExplicit.close();
		derivedFromInferred.close();
	}

	@Override
	protected CloseableIteration<? extends Resource, SailException> getContextIDsInternal()
		throws SailException
	{
		RdfSource source = source(false);
		RdfDataset snapshot = source.snapshot(getIsolationLevel());
		return ClosingRdfIteration.close(snapshot.getContextIDs(), snapshot, source);
	}

	@Override
	protected CloseableIteration<? extends Statement, SailException> getStatementsInternal(Resource subj,
			URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws SailException
	{
		RdfSource source = source(includeInferred);
		RdfDataset snapshot = source.snapshot(getIsolationLevel());
		return ClosingRdfIteration.close(snapshot.get(subj, pred, obj, contexts), snapshot, source);
	}

	@Override
	protected long sizeInternal(Resource... contexts)
		throws SailException
	{
		CloseableIteration<? extends Statement, SailException> iter = getStatementsInternal(null, null, null,
				false, contexts);

		try {
			long size = 0L;

			while (iter.hasNext()) {
				iter.next();
				size++;
			}

			return size;
		}
		finally {
			iter.close();
		}
	}

	@Override
	protected CloseableIteration<? extends Namespace, SailException> getNamespacesInternal()
		throws SailException
	{
		RdfSource source = source(false);
		RdfDataset snapshot = source.snapshot(getIsolationLevel());
		return ClosingRdfIteration.close(snapshot.getNamespaces(), snapshot, source);
	}

	@Override
	protected String getNamespaceInternal(String prefix)
		throws SailException
	{
		RdfSource source = source(false);
		RdfDataset snapshot = source.snapshot(getIsolationLevel());
		try {
			return snapshot.getNamespace(prefix);
		}
		finally {
			snapshot.close();
			source.close();
		}
	}

	@Override
	protected void startTransactionInternal()
		throws SailException
	{
		assert explicitOnlyDatasource == null;
		assert inferredOnlyDatasource == null;
		assert includeInferredDatasource == null;
		explicitOnlyDatasource = derivedFromExplicit.fork();
		inferredOnlyDatasource = derivedFromInferred.fork();
		includeInferredDatasource = new UnionRdfSource(inferredOnlyDatasource, explicitOnlyDatasource);
	}

	@Override
	protected void prepareInternal()
		throws SailException
	{
		if (includeInferredDatasource != null) {
			includeInferredDatasource.prepare();
		}
	}

	@Override
	protected void commitInternal()
		throws SailException
	{
		try {
			if (includeInferredDatasource != null) {
				includeInferredDatasource.flush();
			}
		} finally {
			rollbackInternal();
		}
	}

	@Override
	protected void rollbackInternal()
		throws SailException
	{
		if (includeInferredDatasource != null) {
			includeInferredDatasource.close();
			includeInferredDatasource = null;
			explicitOnlyDatasource = null;
			inferredOnlyDatasource = null;
		}
	}

	@Override
	protected void addStatementInternal(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		RdfSource source = source(false);
		RdfDataset dataset = hasConnectionListeners() ? source.snapshot(getTransactionIsolation()) : null;
		RdfSink sink = source.sink(getTransactionIsolation());
		try {
			if (contexts.length == 0) {
				if (dataset == null || !hasStatement(dataset, subj, pred, obj, null)) {
					sink.approve(subj, pred, obj, null);
					notifyStatementAdded(vf.createStatement(subj, pred, obj));
				}
			}
			else {
				for (Resource ctx : contexts) {
					if (dataset == null || !hasStatement(dataset, subj, pred, obj, ctx)) {
						sink.approve(subj, pred, obj, ctx);
						notifyStatementAdded(vf.createStatement(subj, pred, obj, ctx));
					}
				}
			}
			sink.flush();
		}
		finally {
			sink.close();
			if (dataset != null) {
				dataset.close();
			}
			source.close();
		}
	}

	public boolean addInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		connectionLock.readLock().lock();
		try {
			verifyIsOpen();

			updateLock.lock();
			try {
				verifyIsActive();
				RdfSource source = source(true);
				RdfDataset dataset = hasConnectionListeners() ? source.snapshot(getTransactionIsolation()) : null;
				RdfSink sink = source.sink(getTransactionIsolation());
				try {
					if (contexts.length == 0) {
						if (dataset == null || !hasStatement(dataset, subj, pred, obj, null)) {
							sink.approve(subj, pred, obj, null);
							notifyStatementAdded(vf.createStatement(subj, pred, obj));
						}
					}
					else {
						for (Resource ctx : contexts) {
							if (dataset == null || !hasStatement(dataset, subj, pred, obj, ctx)) {
								sink.approve(subj, pred, obj, ctx);
								notifyStatementAdded(vf.createStatement(subj, pred, obj, ctx));
							}
						}
					}
					sink.flush();
				}
				finally {
					sink.close();
					if (dataset != null) {
						dataset.close();
					}
					source.close();
				}
				return true;
			}
			finally {
				updateLock.unlock();
			}
		}
		finally {
			connectionLock.readLock().unlock();
		}
	}

	@Override
	protected void removeStatementsInternal(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		RdfSource source = source(false);
		RdfDataset dataset = source.snapshot(getIsolationLevel());
		try {
			RdfSink sink = source.sink(getTransactionIsolation());
			try {
				CloseableIteration<? extends Statement, SailException> iter;
				iter = dataset.get(subj, pred, obj, contexts);
				try {
					while (iter.hasNext()) {
						Statement st = iter.next();
						sink.deprecate(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
						notifyStatementRemoved(st);
					}
				}
				finally {
					iter.close();
				}
				sink.flush();
			}
			finally {
				sink.close();
			}
		}
		finally {
			dataset.close();
			source.close();
		}
	}

	public boolean removeInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		connectionLock.readLock().lock();
		try {
			verifyIsOpen();

			updateLock.lock();
			try {
				verifyIsActive();
				boolean statementsRemoved = false;
				RdfSource source = source(true);
				RdfDataset dataset = source.snapshot(getIsolationLevel());
				try {
					RdfSink sink = source.sink(getTransactionIsolation());
					try {
						CloseableIteration<? extends Statement, SailException> iter;
						iter = dataset.get(subj, pred, obj, contexts);
						try {
							while (iter.hasNext()) {
								Statement st = iter.next();
								sink.deprecate(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
								statementsRemoved = true;
								notifyStatementRemoved(st);
							}
						}
						finally {
							iter.close();
						}
						sink.flush();
					}
					finally {
						sink.close();
					}
				}
				finally {
					dataset.close();
					source.close();
				}
				return statementsRemoved;
			}
			finally {
				updateLock.unlock();
			}
		}
		finally {
			connectionLock.readLock().unlock();
		}
	}

	@Override
	protected void clearInternal(Resource... contexts)
		throws SailException
	{
		RdfSource source = source(false);
		RdfDataset dataset = source.snapshot(getIsolationLevel());
		try {
			RdfSink sink = source.sink(getTransactionIsolation());
			try {
				sink.clear(contexts);
				sink.flush();
			}
			finally {
				sink.close();
			}
		}
		finally {
			dataset.close();
			source.close();
		}
	}

	public void clearInferred(Resource... contexts)
		throws SailException
	{
		connectionLock.readLock().lock();
		try {
			verifyIsOpen();

			updateLock.lock();
			try {
				verifyIsActive();
				RdfSource source = source(true);
				RdfDataset dataset = source.snapshot(getIsolationLevel());
				try {
					RdfSink sink = source.sink(getTransactionIsolation());
					try {
						CloseableIteration<? extends Statement, SailException> iter;
						iter = dataset.get(null, null, null, contexts);
						try {
							while (iter.hasNext()) {
								Statement st = iter.next();
								sink.deprecate(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
								notifyStatementRemoved(st);
							}
						}
						finally {
							iter.close();
						}
						sink.flush();
					}
					finally {
						sink.close();
					}
				}
				finally {
					dataset.close();
					source.close();
				}
			}
			finally {
				updateLock.unlock();
			}
		}
		finally {
			connectionLock.readLock().unlock();
		}
	}

	public void flushUpdates()
		throws SailException
	{
		if (!isActiveOperation()) {
			flush();
		}
	}

	@Override
	protected void setNamespaceInternal(String prefix, String name)
		throws SailException
	{
		RdfSource source = source(false);
		RdfSink sink = source.sink(getTransactionIsolation());
		try {
			sink.setNamespace(prefix, name);
			sink.flush();
		}
		finally {
			sink.close();
			source.close();
		}
	}

	@Override
	protected void removeNamespaceInternal(String prefix)
		throws SailException
	{
		RdfSource source = source(false);
		RdfSink sink = source.sink(getTransactionIsolation());
		try {
			sink.removeNamespace(prefix);
			sink.flush();
		}
		finally {
			sink.close();
			source.close();
		}
	}

	@Override
	protected void clearNamespacesInternal()
		throws SailException
	{
		RdfSource source = source(false);
		RdfSink sink = source.sink(getTransactionIsolation());
		try {
			sink.clearNamespaces();
			sink.flush();
		}
		finally {
			sink.close();
			source.close();
		}
	}

	/*-------------------------------------*
	 * Inner class MemEvaluationStatistics *
	 *-------------------------------------*/

	private IsolationLevel getIsolationLevel()
		throws UnknownSailTransactionStateException
	{
		if (isActive()) {
			return super.getTransactionIsolation();
		}
		else {
			return defaultIsolationLevel;
		}
	}

	/**
	 * @return read operation {@link RdfSource}
	 * @throws SailException
	 */
	private RdfSource source(boolean includeinferred)
		throws SailException
	{
		if (includeinferred && isActive()) {
			return new DelegatingRdfSource(includeInferredDatasource, false);
		}
		else if (isActive()) {
			return new DelegatingRdfSource(explicitOnlyDatasource, false);
		}
		else if (includeinferred) {
			return new UnionRdfSource(derivedFromInferred.fork(),
					derivedFromExplicit.fork());
		}
		else {
			return derivedFromExplicit.fork();
		}
	}

	private <T, X extends Exception> CloseableIteration<T, QueryEvaluationException> interlock(
			CloseableIteration<T, QueryEvaluationException> iter, RdfClosable... closes)
	{
		return new ClosingIteration<T, QueryEvaluationException>(iter, closes) {

			protected void handleSailException(SailException e)
				throws QueryEvaluationException
			{
				throw new QueryEvaluationException(e);
			}
		};
	}

	private boolean hasStatement(RdfDataset dataset, Resource subj, URI pred, Value obj, Resource ctx)
		throws SailException
	{
		CloseableIteration<? extends Statement, SailException> iter;
		iter = dataset.get(subj, pred, obj, ctx);
		try {
			return iter.hasNext();
		}
		finally {
			iter.close();
		}
	}
}
