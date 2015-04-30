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

package org.openrdf.sail.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import info.aduna.concurrent.locks.LockingIteration;
import info.aduna.iteration.CloseableIteration;

import org.openrdf.IsolationLevel;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
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
import org.openrdf.sail.SailReadOnlyException;
import org.openrdf.sail.derived.RdfDataset;
import org.openrdf.sail.derived.RdfSink;
import org.openrdf.sail.derived.RdfSource;
import org.openrdf.sail.helpers.DefaultSailChangedEvent;
import org.openrdf.sail.helpers.NotifyingSailConnectionBase;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.memory.model.MemResource;
import org.openrdf.sail.memory.model.MemURI;
import org.openrdf.sail.memory.model.MemValue;
import org.openrdf.sail.memory.model.MemValueFactory;

/**
 * Implementation of a Sail Connection for memory stores.
 * 
 * @author Arjohn Kampman
 * @author jeen
 */
public class MemoryStoreConnection extends NotifyingSailConnectionBase implements InferencerConnection,
		FederatedServiceResolverClient
{

	/*-----------*
	 * Variables *
	 *-----------*/

	protected final MemoryStore store;

	private RdfSource datasource;

	private volatile DefaultSailChangedEvent sailChangedEvent;

	/**
	 * Connection specific resolver.
	 */
	private FederatedServiceResolver federatedServiceResolver;

	/*--------------*
	 * Constructors *
	 *--------------*/

	protected MemoryStoreConnection(MemoryStore store) {
		super(store);
		this.store = store;
		sailChangedEvent = new DefaultSailChangedEvent(store);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public FederatedServiceResolver getFederatedServiceResolver() {
		if (federatedServiceResolver == null)
			return store.getFederatedServiceResolver();
		return federatedServiceResolver;
	}

	public void setFederatedServiceResolver(FederatedServiceResolver resolver) {
		this.federatedServiceResolver = resolver;
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

		RdfDataset rdfDataset = snapshot();
		boolean releaseLock = true;

		try {

			TripleSource tripleSource = new MemTripleSource(store.getValueFactory(), rdfDataset, includeInferred);
			EvaluationStrategy strategy = getEvaluationStrategy(dataset, tripleSource);

			new BindingAssigner().optimize(tupleExpr, dataset, bindings);
			new ConstantOptimizer(strategy).optimize(tupleExpr, dataset, bindings);
			new CompareOptimizer().optimize(tupleExpr, dataset, bindings);
			new ConjunctiveConstraintSplitter().optimize(tupleExpr, dataset, bindings);
			new DisjunctiveConstraintOptimizer().optimize(tupleExpr, dataset, bindings);
			new SameTermFilterOptimizer().optimize(tupleExpr, dataset, bindings);
			new QueryModelNormalizer().optimize(tupleExpr, dataset, bindings);
			new QueryJoinOptimizer(new MemEvaluationStatistics()).optimize(tupleExpr, dataset, bindings);
			// new SubSelectJoinOptimizer().optimize(tupleExpr, dataset, bindings);
			new IterativeEvaluationOptimizer().optimize(tupleExpr, dataset, bindings);
			new FilterOptimizer().optimize(tupleExpr, dataset, bindings);
			new OrderLimitOptimizer().optimize(tupleExpr, dataset, bindings);

			logger.trace("Optimized query model:\n{}", tupleExpr);

			CloseableIteration<BindingSet, QueryEvaluationException> iter;
			iter = strategy.evaluate(tupleExpr, EmptyBindingSet.getInstance());
			iter = new LockingIteration<BindingSet, QueryEvaluationException>(rdfDataset, iter);
			releaseLock = false;
			return iter;
		}
		catch (QueryEvaluationException e) {
			throw new SailException(e);
		}
		finally {
			if (releaseLock) {
				rdfDataset.release();
			}
		}
	}

	protected EvaluationStrategy getEvaluationStrategy(Dataset dataset, TripleSource tripleSource) {
		return new EvaluationStrategyImpl(tripleSource, dataset, getFederatedServiceResolver());
	}

	@Override
	protected void closeInternal()
		throws SailException
	{
		// do nothing
	}

	@Override
	protected CloseableIteration<? extends Resource, SailException> getContextIDsInternal()
		throws SailException
	{
		RdfDataset snapshot = snapshot();
		return new LockingIteration<Resource, SailException>(snapshot, snapshot.getContextIDs());
	}

	@Override
	protected CloseableIteration<? extends Statement, SailException> getStatementsInternal(Resource subj,
			URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws SailException
	{
		RdfDataset snapshot = snapshot();
		if (includeInferred) {
			return new LockingIteration<Statement, SailException>(snapshot, snapshot.getStatements(subj, pred,
					obj, contexts));
		}
		else {
			return new LockingIteration<Statement, SailException>(snapshot, snapshot.getExplicit(subj, pred,
					obj, contexts));
		}
	}

	public boolean hasStatement(Resource subj, URI pred, Value obj, boolean includeInferred,
			Resource... contexts)
		throws SailException
	{
		CloseableIteration<? extends Statement, SailException> iter;
		iter = getStatementsInternal(subj, pred, obj, includeInferred, contexts);
		try {
			return iter.hasNext();
		}
		finally {
			iter.close();
		}
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
		RdfDataset snapshot = snapshot();
		return new LockingIteration<Namespace, SailException>(snapshot, snapshot.getNamespaces());
	}

	@Override
	protected String getNamespaceInternal(String prefix)
		throws SailException
	{
		RdfDataset snapshot = snapshot();
		try {
			return snapshot.getNamespace(prefix);
		}
		finally {
			snapshot.release();
		}
	}

	@Override
	protected void startTransactionInternal()
		throws SailException
	{
		if (!store.isWritable()) {
			throw new SailReadOnlyException("Unable to start transaction: data file is locked or read-only");
		}
		assert datasource == null;
		datasource = store.fork(getTransactionIsolation());
	}

	@Override
	protected void prepareInternal()
		throws SailException
	{
		if (datasource != null) {
			datasource.prepare();
		}
	}

	@Override
	protected void commitInternal()
		throws SailException
	{
		if (datasource != null) {
			try {
				datasource.flush();
			} finally {
				datasource.release();
			}
			datasource = null;

			store.notifySailChanged(sailChangedEvent);

			// create a fresh event object.
			sailChangedEvent = new DefaultSailChangedEvent(store);
		}
	}

	@Override
	protected void rollbackInternal()
		throws SailException
	{
		if (datasource != null) {
			datasource.release();
			datasource = null;
		}
	}

	@Override
	protected void addStatementInternal(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		addStatementInternal(subj, pred, obj, true, contexts);
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
				return addStatementInternal(subj, pred, obj, false, contexts);
			}
			finally {
				updateLock.unlock();
			}
		}
		finally {
			connectionLock.readLock().unlock();
		}
	}

	/**
	 * Adds the specified statement to this MemoryStore.
	 * 
	 * @throws SailException
	 */
	protected boolean addStatementInternal(Resource subj, URI pred, Value obj, boolean explicit,
			Resource... contexts)
		throws SailException
	{
		RdfDataset dataset = hasConnectionListeners() ? datasource.snapshot(getTransactionIsolation()) : null;
		RdfSink sink = datasource.sink(getTransactionIsolation());
		try {
			if (contexts.length == 0) {
				if (dataset == null || !hasStatement(dataset, subj, pred, obj, explicit, null)) {
					if (explicit) {
						sink.addExplicit(subj, pred, obj, null);
					}
					else {
						sink.addInferred(subj, pred, obj, null);
					}
					notifyStatementAdded(store.getValueFactory().createStatement(subj, pred, obj));
				}
			}
			else {
				for (Resource ctx : contexts) {
					if (dataset == null || !hasStatement(dataset, subj, pred, obj, explicit, ctx)) {
						if (explicit) {
							sink.addExplicit(subj, pred, obj, ctx);
						}
						else {
							sink.addInferred(subj, pred, obj, ctx);
						}
						notifyStatementAdded(store.getValueFactory().createStatement(subj, pred, obj, ctx));
					}
				}
			}
			// assume the triple is not yet present in the triple store
			sailChangedEvent.setStatementsAdded(true);
			return true;
		} finally {
			sink.release();
			if (dataset != null) {
				dataset.release();
			}
		}
	}

	@Override
	protected void removeStatementsInternal(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		removeStatementsInternal(subj, pred, obj, true, contexts);
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
				return removeStatementsInternal(subj, pred, obj, false, contexts);
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
		removeStatementsInternal(null, null, null, true, contexts);
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
				removeStatementsInternal(null, null, null, false, contexts);
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

	/**
	 * Removes the statements that match the specified pattern of subject,
	 * predicate, object and context.
	 * 
	 * @param subj
	 *        The subject for the pattern, or <tt>null</tt> for a wildcard.
	 * @param pred
	 *        The predicate for the pattern, or <tt>null</tt> for a wildcard.
	 * @param obj
	 *        The object for the pattern, or <tt>null</tt> for a wildcard.
	 * @param explicit
	 *        Flag indicating whether explicit or inferred statements should be
	 *        removed; <tt>true</tt> removes explicit statements that match the
	 *        pattern, <tt>false</tt> removes inferred statements that match the
	 *        pattern.
	 * @throws SailException
	 */
	protected boolean removeStatementsInternal(Resource subj, URI pred, Value obj, boolean explicit,
			Resource... contexts)
		throws SailException
	{
		boolean statementsRemoved = false;
		RdfDataset dataset = snapshot();
		try {
			RdfSink sink = datasource.sink(getTransactionIsolation());
			try {
				if (explicit) {
					CloseableIteration<? extends Statement, SailException> iter;
					iter = dataset.getExplicit(subj, pred, obj, contexts);
					try {
						while (iter.hasNext()) {
							Statement st = iter.next();
							sink.removeExplicit(st.getSubject(), st.getPredicate(), st.getObject(),
									st.getContext());
							statementsRemoved = true;
							notifyStatementRemoved(st);
						}
					}
					finally {
						iter.close();
					}
				}
				else {
					CloseableIteration<? extends Statement, SailException> iter;
					iter = dataset.getInferred(subj, pred, obj, contexts);
					try {
						while (iter.hasNext()) {
							Statement st = iter.next();
							sink.removeInferred(st.getSubject(), st.getPredicate(), st.getObject(),
									st.getContext());
							statementsRemoved = true;
							notifyStatementRemoved(st);
						}
					}
					finally {
						iter.close();
					}
				}
			} finally {
				sink.release();
			}
		}
		finally {
			dataset.release();
		}
		sailChangedEvent.setStatementsRemoved(true);
		return statementsRemoved;
	}

	@Override
	protected void setNamespaceInternal(String prefix, String name)
		throws SailException
	{
		RdfSink sink = datasource.sink(getTransactionIsolation());
		try {
			sink.setNamespace(prefix, name);
		} finally {
			sink.release();
		}
	}

	@Override
	protected void removeNamespaceInternal(String prefix)
		throws SailException
	{
		RdfSink sink = datasource.sink(getTransactionIsolation());
		try {
			sink.removeNamespace(prefix);
		} finally {
			sink.release();
		}
	}

	@Override
	protected void clearNamespacesInternal()
		throws SailException
	{
		RdfSink sink = datasource.sink(getTransactionIsolation());
		try {
			sink.clearNamespaces();
		} finally {
			sink.release();
		}
	}

	/*-------------------------------------*
	 * Inner class MemEvaluationStatistics *
	 *-------------------------------------*/

	/**
	 * @return
	 * @throws SailException
	 */
	private RdfDataset snapshot()
		throws SailException
	{
		if (isActive()) {
			return datasource.snapshot(getTransactionIsolation());
		}
		else {
			IsolationLevel level = store.getDefaultIsolationLevel();
			return store.fork(level).snapshot(level);
		}
	}

	private boolean hasStatement(RdfDataset dataset, Resource subj, URI pred, Value obj, boolean explicit,
			Resource ctx)
		throws SailException
	{
		CloseableIteration<? extends Statement, SailException> iter;
		if (explicit) {
			iter = dataset.getExplicit(subj, pred, obj, ctx);
			try {
				return iter.hasNext();
			}
			finally {
				iter.close();
			}
		}
		else {
			iter = dataset.getInferred(subj, pred, obj, ctx);
			try {
				return iter.hasNext();
			}
			finally {
				iter.close();
			}
		}
	}

	/**
	 * Uses the MemoryStore's statement sizes to give cost estimates based on the
	 * size of the expected results. This process could be improved with
	 * repository statistics about size and distribution of statements.
	 * 
	 * @author Arjohn Kampman
	 * @author James Leigh
	 */
	protected class MemEvaluationStatistics extends EvaluationStatistics {

		@Override
		protected CardinalityCalculator createCardinalityCalculator() {
			return new MemCardinalityCalculator();
		}

		protected class MemCardinalityCalculator extends CardinalityCalculator {

			@Override
			public double getCardinality(StatementPattern sp) {

				Value subj = getConstantValue(sp.getSubjectVar());
				if (!(subj instanceof Resource)) {
					// can happen when a previous optimizer has inlined a comparison
					// operator.
					// this can cause, for example, the subject variable to be
					// equated to a literal value.
					// See SES-970 / SES-998
					subj = null;
				}
				Value pred = getConstantValue(sp.getPredicateVar());
				if (!(pred instanceof URI)) {
					// can happen when a previous optimizer has inlined a comparison
					// operator. See SES-970 / SES-998
					pred = null;
				}
				Value obj = getConstantValue(sp.getObjectVar());
				Value context = getConstantValue(sp.getContextVar());
				if (!(context instanceof Resource)) {
					// can happen when a previous optimizer has inlined a comparison
					// operator. See SES-970 / SES-998
					context = null;
				}

				MemValueFactory valueFactory = store.getValueFactory();

				// Perform look-ups for value-equivalents of the specified values
				MemResource memSubj = valueFactory.getMemResource((Resource)subj);
				MemURI memPred = valueFactory.getMemURI((URI)pred);
				MemValue memObj = valueFactory.getMemValue(obj);
				MemResource memContext = valueFactory.getMemResource((Resource)context);

				if (subj != null && memSubj == null || pred != null && memPred == null || obj != null
						&& memObj == null || context != null && memContext == null)
				{
					// non-existent subject, predicate, object or context
					return 0.0;
				}

				// Search for the smallest list that can be used by the iterator
				List<Integer> listSizes = new ArrayList<Integer>(4);
				if (memSubj != null) {
					listSizes.add(memSubj.getSubjectStatementCount());
				}
				if (memPred != null) {
					listSizes.add(memPred.getPredicateStatementCount());
				}
				if (memObj != null) {
					listSizes.add(memObj.getObjectStatementCount());
				}
				if (memContext != null) {
					listSizes.add(memContext.getContextStatementCount());
				}

				double cardinality;

				if (listSizes.isEmpty()) {
					// all wildcards
					cardinality = Integer.MAX_VALUE;
				}
				else {
					cardinality = (double)Collections.min(listSizes);

					// List<Var> vars = getVariables(sp);
					// int constantVarCount = countConstantVars(vars);
					//
					// // Subtract 1 from var count as this was used for the list
					// size
					// double unboundVarFactor = (double)(vars.size() -
					// constantVarCount) / (vars.size() - 1);
					//
					// cardinality = Math.pow(cardinality, unboundVarFactor);
				}

				return cardinality;
			}

			protected Value getConstantValue(Var var) {
				if (var != null) {
					return var.getValue();
				}

				return null;
			}
		}
	} // end inner class MemCardinalityCalculator
}
