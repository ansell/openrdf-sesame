/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf.model;

import org.openrdf.model.Value;
import org.openrdf.sail.nativerdf.ValueStoreRevision;

public interface NativeValue extends Value {

	public static final int UNKNOWN_ID = -1;

	/**
	 * Sets the ID that is used for this value in a specific revision of the
	 * value store.
	 */
	public void setInternalID(int id, ValueStoreRevision revision);

	/**
	 * Gets the ID that is used in the native store for this Value.
	 * 
	 * @return The value's ID, or {@link #UNKNOWN_ID} if not yet set.
	 */
	public int getInternalID();

	/**
	 * Gets the revision of the value store that created this value. The value's
	 * internal ID is only valid when it's value store revision is equal to the
	 * value store's current revision.
	 *
	 * @return The revision of the value store that created this value at the
	 * time it last set the value's internal ID.
	 */
	public ValueStoreRevision getValueStoreRevision();
}
