/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.IOException;
import java.util.ArrayList;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.ExceptionConvertingIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.UnionIteration;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.sail.SailException;
import org.openrdf.sail.nativerdf.model.NativeValue;

class NativeTripleSource implements TripleSource {

	/**
	 * 
	 */
	private NativeStore _nativeStore;

	/*
	 * FIXME currently unused as the Native Store does not have an inferencer
	 * yet.
	 */
	private boolean _includeInferred;

	/**
	 * @param store
	 */
	NativeTripleSource(NativeStore store, boolean includeInferred) {
		_nativeStore = store;
		_includeInferred = includeInferred;
	}

	/**
	 * Creates a StatementIterator based on the supplied pattern.
	 * 
	 * @param subj
	 *        The subject of the pattern, or <tt>null</tt> to indicate a
	 *        wildcard.
	 * @param pred
	 *        The predicate of the pattern, or <tt>null</tt> to indicate a
	 *        wildcard.
	 * @param obj
	 *        The object of the pattern, or <tt>null</tt> to indicate a
	 *        wildcard.
	 * @param context
	 *        The context of the pattern, or <tt>null</tt> to indicate a
	 *        wildcard
	 * @param queryNullContext
	 *        Indicates whether just the null context should be queried. If set
	 *        to <tt>true</tt>, the specified context parameter is ignored.
	 * @return A StatementIterator that can be used to iterate over the
	 *         statements that match the specified pattern.
	 */
	private CloseableIteration<? extends Statement, IOException> _getStatements(Resource subj, URI pred,
			Value obj, Resource... contexts)
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		try {
			int subjID = NativeValue.UNKNOWN_ID;
			if (subj != null) {
				subjID = _nativeStore.valueStore.getID(subj);
				if (subjID == NativeValue.UNKNOWN_ID) {
					return new EmptyIteration<Statement, IOException>();
				}
			}

			int predID = NativeValue.UNKNOWN_ID;
			if (pred != null) {
				predID = _nativeStore.valueStore.getID(pred);
				if (predID == NativeValue.UNKNOWN_ID) {
					return new EmptyIteration<Statement, IOException>();
				}
			}

			int objID = NativeValue.UNKNOWN_ID;
			if (obj != null) {
				objID = _nativeStore.valueStore.getID(obj);
				if (objID == NativeValue.UNKNOWN_ID) {
					return new EmptyIteration<Statement, IOException>();
				}
			}

			int contextID = NativeValue.UNKNOWN_ID;

			if (contexts.length > 0) {
				ArrayList<NativeStatementIterator> perContextIterList = new ArrayList<NativeStatementIterator>(
						contexts.length);

				for (Resource context : contexts) {
					if (context != null) {
						contextID = _nativeStore.valueStore.getID(context);

						if (contextID == NativeValue.UNKNOWN_ID) {
							// the specified context is unknown, skip it.
							continue;
						}
					}
					else { // statements with no known context
						contextID = 0;
					}
					perContextIterList.add(new NativeStatementIterator(_nativeStore.tripleStore,
							_nativeStore.valueStore, _nativeStore.valueStore, subj, pred, obj, context, subjID,
							predID, objID, contextID));
				}
				NativeStatementIterator[] array = perContextIterList.toArray(new NativeStatementIterator[perContextIterList.size()]);
				CloseableIteration<? extends Statement, IOException> resultIter = new UnionIteration<Statement, IOException>(
						(Iteration<? extends Statement, IOException>[])array);
				return resultIter;
			}
			else { // no context specified.
				return new NativeStatementIterator(_nativeStore.tripleStore, _nativeStore.valueStore,
						_nativeStore.valueStore, subj, pred, obj, null, subjID, predID, objID, contextID);
			}
		}
		catch (IOException e) {
			throw new RuntimeException("Unable to get statements from NativeTripleSource", e);
		}
	}

	public CloseableIteration<? extends Statement, QueryEvaluationException> getStatements(Resource subj,
			URI pred, Value obj, Resource... contexts)
	{
		return new ExceptionConvertingIteration<Statement, QueryEvaluationException>(_getStatements(subj, pred,
				obj, contexts))
		{

			@Override
			protected QueryEvaluationException convertedException(Exception e)
			{
				if (e instanceof IOException) {
					return new QueryEvaluationException(e);
				}
				else if (e instanceof RuntimeException) {
					throw (RuntimeException)e;
				}
				else {
					assert false : "Unexpected exception type: " + e.getClass();
					throw new IllegalArgumentException("Unexpected exception type", e);
				}
			}
		};
	}

	public CloseableIteration<? extends Statement, SailException> getStatementsSailException(Resource subj,
			URI pred, Value obj, Resource... contexts)
	{
		return new ExceptionConvertingIteration<Statement, SailException>(_getStatements(subj, pred, obj,
				contexts))
		{

			@Override
			protected SailException convertedException(Exception e)
			{
				if (e instanceof SailException) {
					return new SailException(e);
				}
				else if (e instanceof RuntimeException) {
					throw (RuntimeException)e;
				}
				else {
					assert false : "Unexpected exception type: " + e.getClass();
					throw new IllegalArgumentException("Unexpected exception type", e);
				}
			}
		};
	}

	public ValueFactory getValueFactory() {
		return _nativeStore.getValueFactory();
	}
}
