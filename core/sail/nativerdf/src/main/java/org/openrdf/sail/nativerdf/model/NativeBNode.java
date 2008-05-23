/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf.model;

import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.sail.nativerdf.ValueStoreRevision;

public class NativeBNode extends BNodeImpl implements NativeResource {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final long serialVersionUID = 2729080258717960353L;

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueStoreRevision revision;

	private int internalID;

	/*--------------*
	 * Constructors *
	 *--------------*/

	protected NativeBNode(ValueStoreRevision revision, int internalID) {
		super();
		setInternalID(internalID, revision);
	}

	public NativeBNode(ValueStoreRevision revision, String nodeID) {
		this(revision, nodeID, UNKNOWN_ID);
	}

	public NativeBNode(ValueStoreRevision revision, String nodeID, int internalID) {
		super(nodeID);
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

		if (o instanceof NativeBNode && internalID != NativeValue.UNKNOWN_ID) {
			NativeBNode otherNativeBNode = (NativeBNode)o;

			if (otherNativeBNode.internalID != NativeValue.UNKNOWN_ID
					&& revision.equals(otherNativeBNode.revision))
			{
				System.out.println("NativeBNode.equals");
				// NativeBNode's from the same revision of the same native store,
				// with both ID's set
				return internalID == otherNativeBNode.internalID;
			}
		}

		return super.equals(o);
	}

}
