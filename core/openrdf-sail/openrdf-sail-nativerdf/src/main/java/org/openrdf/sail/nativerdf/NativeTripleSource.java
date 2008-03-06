/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.IOException;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.querylogic.TripleSource;
import org.openrdf.sail.SailInternalException;
import org.openrdf.sail.nativerdf.model.NativeValue;
import org.openrdf.util.iterator.CloseableIterator;
import org.openrdf.util.iterator.EmptyIterator;

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
	private CloseableIterator<? extends Statement> _getStatements(Resource subj, URI pred, Value obj,
			Resource context, boolean queryNullContext)
	{
		try {
			int subjID = NativeValue.UNKNOWN_ID;
			if (subj != null) {
				subjID = _nativeStore._valueStore.getID(subj);
				if (subjID == NativeValue.UNKNOWN_ID) {
					return new EmptyIterator<Statement>();
				}
			}

			int predID = NativeValue.UNKNOWN_ID;
			if (pred != null) {
				predID = _nativeStore._valueStore.getID(pred);
				if (predID == NativeValue.UNKNOWN_ID) {
					return new EmptyIterator<Statement>();
				}
			}

			int objID = NativeValue.UNKNOWN_ID;
			if (obj != null) {
				objID = _nativeStore._valueStore.getID(obj);
				if (objID == NativeValue.UNKNOWN_ID) {
					return new EmptyIterator<Statement>();
				}
			}

			int contextID = NativeValue.UNKNOWN_ID;
			if (queryNullContext) {
				contextID = 0;
			}
			else if (context != null) {
				contextID = _nativeStore._valueStore.getID(context);

				if (contextID == NativeValue.UNKNOWN_ID) {
					return new EmptyIterator<Statement>();
				}
			}

			return new NativeStatementIterator(_nativeStore._tripleStore, _nativeStore._valueStore,
					_nativeStore._valueStore, subj, pred, obj, queryNullContext ? null : context, subjID, predID,
					objID, contextID);
		}
		catch (IOException e) {
			throw new SailInternalException(e);
		}
	}

	// Implements RdfSource.getStatements(Resource, URI, Value)
	public CloseableIterator<? extends Statement> getStatements(Resource subj, URI pred, Value obj) {
		return _getStatements(subj, pred, obj, null, false);
	}

	public CloseableIterator<? extends Statement> getNullContextStatements(Resource subj, URI pred, Value obj)
	{
		return _getStatements(subj, pred, obj, null, true);
	}

	public CloseableIterator<? extends Statement> getNamedContextStatements(Resource subj, URI pred,
			Value obj, Resource context)
	{
		return _getStatements(subj, pred, obj, context, false);
	}

	public ValueFactory getValueFactory() {
		return _nativeStore.getValueFactory();
	}
}
