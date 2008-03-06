/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf.model;

import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.sail.nativerdf.ValueStoreRevision;

public class NativeLiteral extends LiteralImpl implements NativeValue {

	/*----------*
	 * Variable *
	 *----------*/

	private ValueStoreRevision _revision;

	private int _id;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public NativeLiteral(ValueStoreRevision revision, String label) {
		this(revision, label, UNKNOWN_ID);
	}

	public NativeLiteral(ValueStoreRevision revision, String label, int id) {
		super(label);
		setInternalID(id, revision);
	}

	public NativeLiteral(ValueStoreRevision revision, String label, String lang) {
		this(revision, label, lang, UNKNOWN_ID);
	}

	public NativeLiteral(ValueStoreRevision revision, String label, String lang, int id) {
		super(label, lang);
		setInternalID(id, revision);
	}

	public NativeLiteral(ValueStoreRevision revision, String label, URI datatype) {
		this(revision, label, datatype, UNKNOWN_ID);
	}

	public NativeLiteral(ValueStoreRevision revision, String label, URI datatype, int id) {
		super(label, datatype);
		setInternalID(id, revision);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setInternalID(int id, ValueStoreRevision revision) {
		_id = id;
		_revision = revision;
	}

	public ValueStoreRevision getValueStoreRevision() {
		return _revision;
	}

	public int getInternalID() {
		return _id;
	}
}
