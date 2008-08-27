/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.IOException;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ExceptionConvertingIteration;

import org.openrdf.StoreException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.EvaluationException;
import org.openrdf.query.algebra.evaluation.TripleSource;

public class NativeTripleSource implements TripleSource {

	/*-----------*
	 * Constants *
	 *-----------*/

	protected final NativeStore nativeStore;

	protected final boolean includeInferred;

	protected final boolean readTransaction;

	/*--------------*
	 * Constructors *
	 *--------------*/

	protected NativeTripleSource(NativeStore store, boolean includeInferred, boolean readTransaction) {
		this.nativeStore = store;
		this.includeInferred = includeInferred;
		this.readTransaction = readTransaction;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public CloseableIteration<? extends Statement, StoreException> getStatements(Resource subj,
			URI pred, Value obj, Resource... contexts)
		throws EvaluationException
	{
		try {
			return new ExceptionConvertingIteration<Statement, StoreException>(
					nativeStore.createStatementIterator(subj, pred, obj, includeInferred, readTransaction,
							contexts))
			{

				@Override
				protected EvaluationException convert(Exception e) {
					if (e instanceof IOException) {
						return new EvaluationException(e);
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
		catch (IOException e) {
			throw new EvaluationException("Unable to get statements", e);
		}
	}

	public ValueFactory getValueFactory() {
		return nativeStore.getValueFactory();
	}
}
