/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf.model;

import org.openrdf.model.impl.URIImpl;
import org.openrdf.sail.nativerdf.ValueStoreRevision;


public class NativeURI extends URIImpl implements NativeResource {

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueStoreRevision _revision;

	private int _id;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public NativeURI(ValueStoreRevision revision, String uri) {
		this(revision, uri, UNKNOWN_ID);
	}

	public NativeURI(ValueStoreRevision revision, String uri, int id) {
		super(uri);
		setInternalID(id, revision);
	}

	public NativeURI(ValueStoreRevision revision, String namespace, String localname) {
		this(revision, namespace + localname);
	}

	public NativeURI(ValueStoreRevision revision, String namespace, String localname, int id) {
		this(revision, namespace + localname, id);
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
