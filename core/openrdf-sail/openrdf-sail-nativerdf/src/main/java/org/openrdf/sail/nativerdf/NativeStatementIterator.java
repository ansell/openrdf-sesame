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
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.nativerdf.btree.BTreeIterator;

/**
 * A NativeStore-specific StatementIterator.
 */
class NativeStatementIterator extends CloseableIterationBase<Statement, IOException> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private BTreeIterator btreeIter;

	private ValueStore valueStore;

	private ValueFactory valueFactory;

	private Resource subj;

	private URI pred;

	private Value obj;

	private Resource context;

	private byte[] nextValue;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new NativeStatementIterator.
	 */
	public NativeStatementIterator(NativeStore nativeStore, Resource subj, URI pred, Value obj,
			Resource context, int subjID, int predID, int objID, int contextID, boolean includeInferred)
		throws IOException
	{
		this(nativeStore.getTripleStore(), nativeStore.getValueStore(), nativeStore.getValueFactory(), subj,
				pred, obj, context, subjID, predID, objID, contextID, includeInferred);
	}

	/**
	 * Creates a new NativeStatementIterator.
	 */
	public NativeStatementIterator(TripleStore tripleStore, ValueStore valueStore, ValueFactory valueFactory,
			Resource subj, URI pred, Value obj, Resource context, int subjID, int predID, int objID,
			int contextID, boolean includeInferred)
		throws IOException
	{
		this.valueStore = valueStore;
		this.valueFactory = valueFactory;

		this.subj = subj;
		this.pred = pred;
		this.obj = obj;
		this.context = context;

		if (includeInferred) {
			this.btreeIter = tripleStore.getTriples(subjID, predID, objID, contextID);
		}
		else {
			// explicit only
			this.btreeIter = tripleStore.getTriples(subjID, predID, objID, contextID, true);
		}

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

		Resource s = subj;
		if (s == null) {
			int subjID = ByteArrayUtil.getInt(nextValue, TripleStore.SUBJ_IDX);
			s = (Resource)valueStore.getValue(subjID);
		}

		URI p = pred;
		if (p == null) {
			int predID = ByteArrayUtil.getInt(nextValue, TripleStore.PRED_IDX);
			p = (URI)valueStore.getValue(predID);
		}

		Value o = obj;
		if (o == null) {
			int objID = ByteArrayUtil.getInt(nextValue, TripleStore.OBJ_IDX);
			o = (Value)valueStore.getValue(objID);
		}

		Resource c = context;
		if (c == null) {
			int contextID = ByteArrayUtil.getInt(nextValue, TripleStore.CONTEXT_IDX);
			if (contextID != 0) {
				c = (Resource)valueStore.getValue(contextID);
			}
		}

		nextValue = btreeIter.next();

		return valueFactory.createStatement(s, p, o, c);
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
		super.handleClose();
	}
}
