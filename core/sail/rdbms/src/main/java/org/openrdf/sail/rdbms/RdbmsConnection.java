/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms;

import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.locks.Lock;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.DefaultSailChangedEvent;
import org.openrdf.sail.helpers.SailConnectionBase;
import org.openrdf.sail.rdbms.evaluation.RdbmsEvaluationFactory;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.iteration.NamespaceIteration;
import org.openrdf.sail.rdbms.iteration.RdbmsResourceIteration;
import org.openrdf.sail.rdbms.managers.NamespaceManager;
import org.openrdf.sail.rdbms.model.RdbmsResource;
import org.openrdf.sail.rdbms.model.RdbmsURI;
import org.openrdf.sail.rdbms.model.RdbmsValue;
import org.openrdf.sail.rdbms.optimizers.RdbmsQueryOptimizer;

/**
 * Coordinates the triple store, namespace manager, optimizer, and evaluation
 * strategy into the {@link SailConnection} interface.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsConnection extends SailConnectionBase {
	private RdbmsStore sail;
	private RdbmsValueFactory vf;
	private RdbmsTripleRepository triples;
	private NamespaceManager namespaces;
	private RdbmsQueryOptimizer optimizer;
	private RdbmsEvaluationFactory factory;
	private Lock lock;
	private boolean locked;

	public RdbmsConnection(RdbmsStore sail, RdbmsTripleRepository triples) {
		super(sail);
		this.sail = sail;
		this.vf = sail.getValueFactory();
		this.triples = triples;
	}

	public void setNamespaces(NamespaceManager namespaces) {
		this.namespaces = namespaces;
	}

	public void setRdbmsQueryOptimizer(RdbmsQueryOptimizer optimizer) {
		this.optimizer = optimizer;
	}

	public void setRdbmsEvaluationFactory(RdbmsEvaluationFactory factory) {
		this.factory = factory;
	}

	public void setLock(Lock lock) {
		this.lock = lock;
	}

	@Override
	protected void addStatementInternal(Resource subj, URI pred, Value obj,
			Resource... contexts) throws SailException {
		try {
			if (contexts.length == 0) {
				triples.add(vf.createStatement(subj, pred, obj));
			} else {
				for (Resource ctx : contexts) {
					triples.add(vf.createStatement(subj, pred, obj, ctx));
				}
			}
		} catch (SQLException e) {
			throw new RdbmsException(e);
		}
		catch (InterruptedException e) {
			throw new RdbmsException(e);
		}
	}

	@Override
	protected void clearInternal(Resource... contexts) throws SailException {
		removeStatementsInternal(null, null, null, contexts);
	}

	@Override
	protected void closeInternal() throws SailException {
		try {
			triples.close();
		} catch (SQLException e) {
			throw new RdbmsException(e);
		} finally {
			unlock();
		}
	}

	@Override
	protected void commitInternal() throws SailException {
		try {
			triples.commit();
			unlock();
		} catch (SQLException e) {
			throw new RdbmsException(e);
		}
		catch (InterruptedException e) {
			throw new RdbmsException(e);
		}

		sail.notifySailChanged(triples.getSailChangedEvent());
		// create a fresh event object.
		triples.setSailChangedEvent(new DefaultSailChangedEvent(sail));
	}

	@Override
	protected RdbmsResourceIteration getContextIDsInternal()
			throws SailException {
		try {
			return triples.findContexts();
		} catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

	@Override
	protected CloseableIteration<? extends Statement, SailException> getStatementsInternal(
			Resource subj, URI pred, Value obj, boolean includeInferred,
			Resource... contexts) throws SailException {
		RdbmsResource s = vf.asRdbmsResource(subj);
		RdbmsURI p = vf.asRdbmsURI(pred);
		RdbmsValue o = vf.asRdbmsValue(obj);
		RdbmsResource[] c = vf.asRdbmsResource(contexts);
		return triples.find(s, p, o, c);
	}

	@Override
	protected void removeStatementsInternal(Resource subj, URI pred, Value obj,
			Resource... contexts) throws SailException {
		RdbmsResource s = vf.asRdbmsResource(subj);
		RdbmsURI p = vf.asRdbmsURI(pred);
		RdbmsValue o = vf.asRdbmsValue(obj);
		RdbmsResource[] c = vf.asRdbmsResource(contexts);
		triples.remove(s, p, o, c);
	}

	@Override
	protected void rollbackInternal() throws SailException {
		try {
			triples.rollback();
		} catch (SQLException e) {
			throw new RdbmsException(e);
		} finally {
			unlock();
		}
	}

	@Override
	protected CloseableIteration<BindingSet, QueryEvaluationException> evaluateInternal(
			TupleExpr expr, Dataset dataset, BindingSet bindings,
			boolean includeInferred) throws SailException {
		triples.flush();
		try {
			TupleExpr tupleExpr;
			EvaluationStrategy strategy;
			strategy = factory.createRdbmsEvaluation(dataset);
			tupleExpr = optimizer.optimize(expr, dataset, bindings, strategy);
			return strategy.evaluate(tupleExpr, bindings);
		} catch (QueryEvaluationException e) {
			throw new SailException(e);
		}
	}

	@Override
	protected void clearNamespacesInternal() throws SailException {
		namespaces.clearPrefixes();
	}

	@Override
	protected String getNamespaceInternal(String prefix) throws SailException {
		Namespace ns = namespaces.findByPrefix(prefix);
		if (ns == null)
			return null;
		return ns.getName();
	}

	@Override
	protected CloseableIteration<? extends Namespace, SailException> getNamespacesInternal()
			throws SailException {
		Collection<? extends Namespace> ns = namespaces.getNamespacesWithPrefix();
		return new NamespaceIteration(ns.iterator());
	}

	@Override
	protected void removeNamespaceInternal(String prefix) throws SailException {
		namespaces.removePrefix(prefix);
	}

	@Override
	protected void setNamespaceInternal(String prefix, String name)
			throws SailException {
		namespaces.setPrefix(prefix, name);
	}

	@Override
	protected long sizeInternal(Resource... contexts) throws SailException {
		try {
			return triples.size(vf.asRdbmsResource(contexts));
		} catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

	@Override
	protected void startTransactionInternal() throws SailException {
		try {
			lock();
			triples.begin();
		} catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

	@Override
	protected void finalize()
		throws Throwable
	{
		unlock();
		super.finalize();
	}

	private void lock() {
		if (lock != null) {
			lock.lock();
			locked = true;
		}
	}

	private void unlock() {
		if (locked && lock != null) {
			locked = false;
			lock.unlock();
		}
	}

}
