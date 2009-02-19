/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf.model;

import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.sail.nativerdf.ValueStoreRevision;

public class NativeLiteral extends LiteralImpl implements NativeValue {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final long serialVersionUID = 5198968663650168819L;

	/*----------*
	 * Variable *
	 *----------*/

	private ValueStoreRevision revision;

	private int internalID;

	/*--------------*
	 * Constructors *
	 *--------------*/

	protected NativeLiteral(ValueStoreRevision revision, int internalID) {
		super();
		setInternalID(internalID, revision);
	}

	public NativeLiteral(ValueStoreRevision revision, String label) {
		this(revision, label, UNKNOWN_ID);
	}

	public NativeLiteral(ValueStoreRevision revision, String label, int internalID) {
		super(label);
		setInternalID(internalID, revision);
	}

	public NativeLiteral(ValueStoreRevision revision, String label, String lang) {
		this(revision, label, lang, UNKNOWN_ID);
	}

	public NativeLiteral(ValueStoreRevision revision, String label, String lang, int internalID) {
		super(label, lang);
		setInternalID(internalID, revision);
	}

	public NativeLiteral(ValueStoreRevision revision, String label, URI datatype) {
		this(revision, label, datatype, UNKNOWN_ID);
	}

	public NativeLiteral(ValueStoreRevision revision, String label, URI datatype, int internalID) {
		super(label, datatype);
		setInternalID(internalID, revision);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setInternalID(int internalID, ValueStoreRevision revision) {
		this.internalID = internalID;
		this.revision = revision;
	}

	public ValueStoreRevision getValueStoreRevision() {
		return revision;
	}

	public int getInternalID() {
		return internalID;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o instanceof NativeLiteral && internalID != NativeValue.UNKNOWN_ID) {
			NativeLiteral otherNativeLiteral = (NativeLiteral)o;

			if (otherNativeLiteral.internalID != NativeValue.UNKNOWN_ID
					&& revision.equals(otherNativeLiteral.revision))
			{
				// NativeLiteral's from the same revision of the same native store,
				// with both ID's set
				return internalID == otherNativeLiteral.internalID;
			}
		}

		return super.equals(o);
	}
}
