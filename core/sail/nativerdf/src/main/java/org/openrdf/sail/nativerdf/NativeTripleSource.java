/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.IOException;

import info.aduna.iteration.ExceptionConvertingIteration;

import org.openrdf.StoreException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.Cursor;
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

	public Cursor<? extends Statement> getStatements(Resource subj,
			URI pred, Value obj, Resource... contexts)
		throws EvaluationException
	{
		try {
			return nativeStore.createStatementIterator(subj, pred, obj, includeInferred, readTransaction,
					contexts);
		}
		catch (IOException e) {
			throw new EvaluationException("Unable to get statements", e);
		}
	}

	public ValueFactory getValueFactory() {
		return nativeStore.getValueFactory();
	}
}
