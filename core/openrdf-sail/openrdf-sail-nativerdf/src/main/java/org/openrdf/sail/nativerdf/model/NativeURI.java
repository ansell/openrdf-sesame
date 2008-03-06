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

	/**
	 * 
	 */
	private static final long serialVersionUID = -4434961231872778488L;

	private ValueStoreRevision revision;

	private int id;

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
		this.id = id;
		this.revision = revision;
	}

	public ValueStoreRevision getValueStoreRevision() {
		return revision;
	}

	public int getInternalID() {
		return id;
	}
	
	
}
