/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf.model;

import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.sail.nativerdf.ValueStoreRevision;

public class NativeBNode extends BNodeImpl implements NativeResource {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * 
	 */
	private static final long serialVersionUID = -6909531767964492411L;

	private ValueStoreRevision revision;

	private int id;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public NativeBNode(ValueStoreRevision revision, String nodeID) {
		this(revision, nodeID, UNKNOWN_ID);
	}

	public NativeBNode(ValueStoreRevision revision, String nodeID, int id) {
		super(nodeID);
		setInternalID(id, revision);
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
