/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.SailInternalException;
import org.openrdf.sail.nativerdf.btree.BTreeIterator;
import org.openrdf.util.ByteArrayUtil;
import org.openrdf.util.iterator.CloseableIteratorBase;


/**
 * A NativeStore-specific StatementIterator.
 */
class NativeStatementIterator extends CloseableIteratorBase<Statement> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private BTreeIterator _btreeIter;
	private ValueStore _valueStore;
	private ValueFactory _valueFactory;

	private Resource _subj;
	private URI _pred;
	private Value _obj;
	private Resource _context;

	private byte[] _nextValue;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new NativeStatementIterator.
	 */
	public NativeStatementIterator(
		TripleStore tripleStore, ValueStore valueStore, ValueFactory valueFactory,
		Resource subj, URI pred, Value obj, Resource context,
		int subjID, int predID, int objID, int contextID)
		throws IOException
	{
		_valueStore = valueStore;
		_valueFactory = valueFactory;

		_subj = subj;
		_pred = pred;
		_obj = obj;
		_context = context;

		_btreeIter = tripleStore.getTriples(subjID, predID, objID, contextID);

		_nextValue = _btreeIter.next();
	}

	/*---------*
	 * Methods *
	 *---------*/

	public boolean hasNext() {
		return _nextValue != null;
	}

	public Statement next() {
		if (_nextValue == null) {
			throw new NoSuchElementException();
		}

		try {
			Resource subj = _subj;
			if (subj == null) {
				int subjID = ByteArrayUtil.getInt(_nextValue, TripleStore.SUBJ_IDX);
				subj = (Resource)_valueStore.getValue(subjID);
			}

			URI pred = _pred;
			if (pred == null) {
				int predID = ByteArrayUtil.getInt(_nextValue, TripleStore.PRED_IDX);
				pred = (URI)_valueStore.getValue(predID);
			}

			Value obj = _obj;
			if (obj == null) {
				int objID  = ByteArrayUtil.getInt(_nextValue, TripleStore.OBJ_IDX);
				obj = (Value)_valueStore.getValue(objID);
			}
	
			Resource context = _context;
			if (context == null) {
				int contextID = ByteArrayUtil.getInt(_nextValue, TripleStore.CONTEXT_IDX);
				if (contextID != 0) {
					context = (Resource)_valueStore.getValue(contextID);
				}
			}

			_nextValue = _btreeIter.next();

			return _valueFactory.createStatement(subj, pred, obj, context);
		}
		catch (IOException e) {
			throw new SailInternalException(e);
		}
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	public void close() {
		if (!isClosed()) {
			_nextValue = null;
		
			try {
				_btreeIter.close();
			}
			catch (IOException e) {
				throw new SailInternalException(e);
			}
		}
		
		super.close();
	}
}
