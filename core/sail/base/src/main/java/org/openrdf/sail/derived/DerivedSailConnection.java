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

import java.util.HashMap;
import java.util.Map;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.IsolationLevel;
import org.openrdf.IsolationLevels;
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
import org.openrdf.sail.UpdateContext;
import org.openrdf.sail.helpers.NotifyingSailConnectionBase;
import org.openrdf.sail.helpers.SailBase;
import org.openrdf.sail.inferencer.InferencerConnection;

/**
 * @author James Leigh
 */
public abstract class DerivedSailConnection extends NotifyingSailConnectionBase implements InferencerConnection,
		FederatedServiceResolverClient
{

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The state of store for outstanding operations.
	 */
	private final Map<UpdateContext, RdfDataset> datasets = new HashMap<UpdateContext, RdfDataset>();

	/**
	 * Outstanding changes that are underway, but not yet realized, by an active
	 * operation.
	 */
	private final Map<UpdateContext, RdfSink> sinks = new HashMap<UpdateContext, RdfSink>();

	private final ValueFactory vf;

	private final RdfStore store;

	private final IsolationLevel defaultIsolationLevel;

	private RdfBranch explicitOnlyDatasource;

	private RdfBranch inferredOnlyDatasource;

	private RdfBranch includeInferredDatasource;

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
		this.store = store;
		this.defaultIsolationLevel = sail.getDefaultIsolationLevel();
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

		RdfBranch branch = branch(includeInferred);
		RdfDataset rdfDataset = branch.dataset(getIsolationLevel());
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
			new QueryJoinOptimizer(store.getEvaluationStatistics()).optimize(tupleExpr, dataset, bindings);
			// new SubSelectJoinOptimizer().optimize(tupleExpr, dataset, bindings);
			new IterativeEvaluationOptimizer().optimize(tupleExpr, dataset, bindings);
			new FilterOptimizer().optimize(tupleExpr, dataset, bindings);
			new OrderLimitOptimizer().optimize(tupleExpr, dataset, bindings);

			logger.trace("Optimized query model:\n{}", tupleExpr);

			CloseableIteration<BindingSet, QueryEvaluationException> iter;
			iter = strategy.evaluate(tupleExpr, EmptyBindingSet.getInstance());
			iter = interlock(iter, rdfDataset, branch);
			releaseLock = false;
			return iter;
		}
		catch (QueryEvaluationException e) {
			throw new SailException(e);
		}
		finally {
			if (releaseLock) {
				rdfDataset.close();
				branch.close();
			}
		}
	}

	@Override
	protected void closeInternal()
		throws SailException
	{
		// no-op
	}

	@Override
	protected CloseableIteration<? extends Resource, SailException> getContextIDsInternal()
		throws SailException
	{
		RdfBranch branch = branch(false);
		RdfDataset snapshot = branch.dataset(getIsolationLevel());
		return ClosingRdfIteration.close(snapshot.getContextIDs(), snapshot, branch);
	}

	@Override
	protected CloseableIteration<? extends Statement, SailException> getStatementsInternal(Resource subj,
			URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws SailException
	{
		RdfBranch branch = branch(includeInferred);
		RdfDataset snapshot = branch.dataset(getIsolationLevel());
		return ClosingRdfIteration.close(snapshot.get(subj, pred, obj, contexts), snapshot, branch);
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
		RdfBranch branch = branch(false);
		RdfDataset snapshot = branch.dataset(getIsolationLevel());
		return ClosingRdfIteration.close(snapshot.getNamespaces(), snapshot, branch);
	}

	@Override
	protected String getNamespaceInternal(String prefix)
		throws SailException
	{
		RdfBranch branch = branch(false);
		RdfDataset snapshot = branch.dataset(getIsolationLevel());
		try {
			return snapshot.getNamespace(prefix);
		}
		finally {
			snapshot.close();
			branch.close();
		}
	}

	@Override
	protected void startTransactionInternal()
		throws SailException
	{
		assert explicitOnlyDatasource == null;
		assert inferredOnlyDatasource == null;
		assert includeInferredDatasource == null;
		IsolationLevel level = getTransactionIsolation();
		if (!IsolationLevels.NONE.isCompatibleWith(level)) {
			// only create transaction branches if transaction is isolated
			explicitOnlyDatasource = store.getExplicitRdfSource(level).fork();
			inferredOnlyDatasource = store.getInferredRdfSource(level).fork();
			includeInferredDatasource = new UnionRdfBranch(inferredOnlyDatasource, explicitOnlyDatasource);
		}
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
		synchronized (sinks) {
			sinks.clear();
		}
		if (includeInferredDatasource != null) {
			includeInferredDatasource.close();
			includeInferredDatasource = null;
			explicitOnlyDatasource = null;
			inferredOnlyDatasource = null;
		}
	}

	@Override
	public void startUpdate(UpdateContext op)
		throws SailException
	{
		if (op != null) {
			IsolationLevel level = getIsolationLevel();
			if (!isActiveOperation() || isActive()
					&& !level.isCompatibleWith(IsolationLevels.SNAPSHOT_READ))
			{
				flush();
			}
			synchronized (sinks) {
				assert !sinks.containsKey(op);
				RdfSource source = op.isIncludeInferred() ? new UnionRdfBranch(explicitOnlyDatasource,
						inferredOnlyDatasource) : explicitOnlyDatasource;
				datasets.put(op, source.dataset(level));
				sinks.put(op, source.sink(level));
			}
		}
	}

	@Override
	public void addStatement(UpdateContext op, Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		verifyIsOpen();
		verifyIsActive();
		synchronized (sinks) {
			if (op == null && !sinks.containsKey(op)) {
				RdfBranch branch = branch(false);
				datasets.put(op, branch.dataset(getIsolationLevel()));
				sinks.put(op, branch.sink(getIsolationLevel()));
			}
			assert sinks.containsKey(op);
			add(subj, pred, obj, datasets.get(op), sinks.get(op), contexts);
		}
		addStatementInternal(subj, pred, obj, contexts);
	}

	@Override
	public void removeStatement(UpdateContext op, Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		verifyIsOpen();
		verifyIsActive();
		synchronized (sinks) {
			if (op == null && !sinks.containsKey(op)) {
				RdfBranch branch = branch(false);
				datasets.put(op, branch.dataset(getIsolationLevel()));
				sinks.put(op, branch.sink(getIsolationLevel()));
			}
			assert sinks.containsKey(op);
			remove(subj, pred, obj, datasets.get(op), sinks.get(op), contexts);
		}
		removeStatementsInternal(subj, pred, obj, contexts);
	}

	@Override
	protected void endUpdateInternal(UpdateContext op)
		throws SailException
	{
		synchronized (sinks) {
			RdfSink sink = sinks.remove(op);
			if (sink != null) {
				try {
					sink.flush();
				} finally {
					sink.close();
					datasets.remove(op).close();
				}
			}
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
				RdfBranch branch = branch(true);
				RdfDataset dataset = hasConnectionListeners() ? branch.dataset(getTransactionIsolation()) : null;
				RdfSink sink = branch.sink(getTransactionIsolation());
				try {
					add(subj, pred, obj, dataset, sink, contexts);
					sink.flush();
				}
				finally {
					sink.close();
					if (dataset != null) {
						dataset.close();
					}
					branch.close();
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

	private void add(Resource subj, URI pred, Value obj, RdfDataset dataset, RdfSink sink,
			Resource... contexts)
		throws SailException
	{
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
				RdfBranch branch = branch(true);
				RdfDataset dataset = branch.dataset(getIsolationLevel());
				try {
					RdfSink sink = branch.sink(getTransactionIsolation());
					try {
						statementsRemoved |= remove(subj, pred, obj, dataset, sink, contexts);
						sink.flush();
					}
					finally {
						sink.close();
					}
				}
				finally {
					dataset.close();
					branch.close();
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

	private boolean remove(Resource subj, URI pred, Value obj, RdfDataset dataset,
			RdfSink sink, Resource... contexts)
		throws SailException
	{
		boolean statementsRemoved = false;
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
		return statementsRemoved;
	}

	@Override
	protected void clearInternal(Resource... contexts)
		throws SailException
	{
		RdfBranch branch = branch(false);
		RdfDataset dataset = branch.dataset(getIsolationLevel());
		try {
			RdfSink sink = branch.sink(getTransactionIsolation());
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
			branch.close();
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
				RdfBranch branch = branch(true);
				RdfDataset dataset = branch.dataset(getIsolationLevel());
				try {
					RdfSink sink = branch.sink(getTransactionIsolation());
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
					branch.close();
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
		RdfBranch branch = branch(false);
		RdfSink sink = branch.sink(getTransactionIsolation());
		try {
			sink.setNamespace(prefix, name);
			sink.flush();
		}
		finally {
			sink.close();
			branch.close();
		}
	}

	@Override
	protected void removeNamespaceInternal(String prefix)
		throws SailException
	{
		RdfBranch branch = branch(false);
		RdfSink sink = branch.sink(getTransactionIsolation());
		try {
			sink.removeNamespace(prefix);
			sink.flush();
		}
		finally {
			sink.close();
			branch.close();
		}
	}

	@Override
	protected void clearNamespacesInternal()
		throws SailException
	{
		RdfBranch branch = branch(false);
		RdfSink sink = branch.sink(getTransactionIsolation());
		try {
			sink.clearNamespaces();
			sink.flush();
		}
		finally {
			sink.close();
			branch.close();
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
	 * @return read operation {@link RdfBranch}
	 * @throws SailException
	 */
	private RdfBranch branch(boolean includeinferred)
		throws SailException
	{
		boolean active = isActive();
		IsolationLevel level = getIsolationLevel();
		boolean isolated = !IsolationLevels.NONE.isCompatibleWith(level);
		if (includeinferred && active && isolated) {
			// use the transaction branch
			return new DelegatingRdfBranch(includeInferredDatasource, false);
		}
		else if (active && isolated) {
			// use the transaction branch
			return new DelegatingRdfBranch(explicitOnlyDatasource, false);
		}
		else if (includeinferred && active) {
			// don't actually branch source
			return new UnionRdfBranch(new RdfNotBranchedSource(store.getInferredRdfSource(level)),
					new RdfNotBranchedSource(store.getExplicitRdfSource(level)));
		}
		else if (active) {
			// don't actually branch source
			return new RdfNotBranchedSource(store.getExplicitRdfSource(level));
		}
		else if (includeinferred) {
			// create a new branch for read operation
			return new UnionRdfBranch(store.getInferredRdfSource(level).fork(), store.getExplicitRdfSource(level).fork());
		}
		else {
			// create a new branch for read operation
			return store.getExplicitRdfSource(level).fork();
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
