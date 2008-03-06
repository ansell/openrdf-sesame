/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
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

public class NativeTripleSource implements TripleSource {

	/*-----------*
	 * Constants *
	 *-----------*/

	protected final NativeStore nativeStore;

	protected final boolean includeInferred;

	/*--------------*
	 * Constructors *
	 *--------------*/

	protected NativeTripleSource(NativeStore store, boolean includeInferred) {
		this.nativeStore = store;
		this.includeInferred = includeInferred;
	}

	/*---------*
	 * Methods *
	 *---------*/

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
	protected CloseableIteration<? extends Statement, IOException> getStatementsInternal(Resource subj,
			URI pred, Value obj, Resource... contexts)
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		try {
			int subjID = NativeValue.UNKNOWN_ID;
			if (subj != null) {
				subjID = nativeStore.getValueStore().getID(subj);
				if (subjID == NativeValue.UNKNOWN_ID) {
					return new EmptyIteration<Statement, IOException>();
				}
			}

			int predID = NativeValue.UNKNOWN_ID;
			if (pred != null) {
				predID = nativeStore.getValueStore().getID(pred);
				if (predID == NativeValue.UNKNOWN_ID) {
					return new EmptyIteration<Statement, IOException>();
				}
			}

			int objID = NativeValue.UNKNOWN_ID;
			if (obj != null) {
				objID = nativeStore.getValueStore().getID(obj);
				if (objID == NativeValue.UNKNOWN_ID) {
					return new EmptyIteration<Statement, IOException>();
				}
			}

			if (contexts.length > 0) {
				ArrayList<NativeStatementIterator> perContextIterList = new ArrayList<NativeStatementIterator>(
						contexts.length);

				for (Resource context : contexts) {
					int contextID;
					if (context == null) {
						// statements without context
						contextID = 0;
					}
					else {
						contextID = nativeStore.getValueStore().getID(context);

						if (contextID == NativeValue.UNKNOWN_ID) {
							// the specified context is unknown, skip it.
							continue;
						}
					}

					perContextIterList.add(new NativeStatementIterator(nativeStore, subj, pred, obj, context,
							subjID, predID, objID, contextID, includeInferred));
				}

				NativeStatementIterator[] array = perContextIterList.toArray(new NativeStatementIterator[perContextIterList.size()]);

				return new UnionIteration<Statement, IOException>(
						(Iteration<? extends Statement, IOException>[])array);
			}
			else { // no context specified.
				return new NativeStatementIterator(nativeStore, subj, pred, obj, null, subjID, predID, objID,
						NativeValue.UNKNOWN_ID, includeInferred);
			}
		}
		catch (IOException e) {
			throw new RuntimeException("Unable to get statements from NativeTripleSource", e);
		}
	}

	public CloseableIteration<? extends Statement, QueryEvaluationException> getStatements(Resource subj,
			URI pred, Value obj, Resource... contexts)
	{
		return new ExceptionConvertingIteration<Statement, QueryEvaluationException>(getStatementsInternal(
				subj, pred, obj, contexts))
		{

			@Override
			protected QueryEvaluationException convert(Exception e)
			{
				if (e instanceof IOException) {
					return new QueryEvaluationException(e);
				}
				else if (e instanceof RuntimeException) {
					throw (RuntimeException)e;
				}
				else if (e == null) {
					throw new IllegalArgumentException("e must not be null");
				}
				else {
					throw new IllegalArgumentException("Unexpected exception type: " + e.getClass());
				}
			}
		};
	}

	public CloseableIteration<? extends Statement, SailException> getStatementsSailException(Resource subj,
			URI pred, Value obj, Resource... contexts)
	{
		return new ExceptionConvertingIteration<Statement, SailException>(getStatementsInternal(subj, pred,
				obj, contexts))
		{

			@Override
			protected SailException convert(Exception e)
			{
				if (e instanceof SailException) {
					return new SailException(e);
				}
				else if (e instanceof RuntimeException) {
					throw (RuntimeException)e;
				}
				else if (e == null) {
					throw new IllegalArgumentException("e must not be null");
				}
				else {
					throw new IllegalArgumentException("Unexpected exception type: " + e.getClass());
				}
			}
		};
	}

	public ValueFactory getValueFactory() {
		return nativeStore.getValueFactory();
	}
}
