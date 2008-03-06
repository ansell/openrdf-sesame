/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.IOException;
import java.util.NoSuchElementException;

import info.aduna.io.ByteArrayUtil;
import info.aduna.iteration.CloseableIterationBase;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.nativerdf.btree.BTreeIterator;

/**
 * A statement iterator that wraps a BTreeIterator containing statement records
 * and translates these records to {@link Statement} objects.
 */
class NativeStatementIterator extends CloseableIterationBase<Statement, IOException> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private BTreeIterator btreeIter;

	private ValueStore valueStore;

	private byte[] nextValue;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new NativeStatementIterator.
	 */
	public NativeStatementIterator(BTreeIterator btreeIter, ValueStore valueStore)
		throws IOException
	{
		this.btreeIter = btreeIter;
		this.valueStore = valueStore;

		this.nextValue = btreeIter.next();
	}

	/*---------*
	 * Methods *
	 *---------*/

	public boolean hasNext() {
		return nextValue != null;
	}

	public Statement next()
		throws IOException
	{
		if (nextValue == null) {
			throw new NoSuchElementException();
		}

		int subjID = ByteArrayUtil.getInt(nextValue, TripleStore.SUBJ_IDX);
		Resource subj = (Resource)valueStore.getValue(subjID);

		int predID = ByteArrayUtil.getInt(nextValue, TripleStore.PRED_IDX);
		URI pred = (URI)valueStore.getValue(predID);

		int objID = ByteArrayUtil.getInt(nextValue, TripleStore.OBJ_IDX);
		Value obj = valueStore.getValue(objID);

		Resource context = null;
		int contextID = ByteArrayUtil.getInt(nextValue, TripleStore.CONTEXT_IDX);
		if (contextID != 0) {
			context = (Resource)valueStore.getValue(contextID);
		}

		nextValue = btreeIter.next();

		return valueStore.createStatement(subj, pred, obj, context);
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void handleClose()
		throws IOException
	{
		nextValue = null;
		btreeIter.close();
	}
}
