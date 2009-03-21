/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf.model;

import org.openrdf.model.impl.URIImpl;
import org.openrdf.sail.nativerdf.ValueStoreRevision;

public class NativeURI extends URIImpl implements NativeResource {

	private static final long serialVersionUID = -5888138591826143179L;

	/*-----------*
	 * Constants *
	 *-----------*/

	private volatile ValueStoreRevision revision;

	private volatile int internalID;

	/*--------------*
	 * Constructors *
	 *--------------*/

	protected NativeURI(ValueStoreRevision revision, int internalID) {
		super();
		setInternalID(internalID, revision);
	}

	public NativeURI(ValueStoreRevision revision, String uri) {
		this(revision, uri, UNKNOWN_ID);
	}

	public NativeURI(ValueStoreRevision revision, String uri, int internalID) {
		super(uri);
		setInternalID(internalID, revision);
	}

	public NativeURI(ValueStoreRevision revision, String namespace, String localname) {
		this(revision, namespace + localname);
	}

	public NativeURI(ValueStoreRevision revision, String namespace, String localname, int internalID) {
		this(revision, namespace + localname, internalID);
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

		if (o instanceof NativeURI && internalID != NativeValue.UNKNOWN_ID) {
			NativeURI otherNativeURI = (NativeURI)o;

			if (otherNativeURI.internalID != NativeValue.UNKNOWN_ID && revision.equals(otherNativeURI.revision))
			{
				// NativeURI's from the same revision of the same native store, with
				// both ID's set
				return internalID == otherNativeURI.internalID;
			}
		}

		return super.equals(o);
	}
}
