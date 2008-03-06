/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.Serializable;

import org.openrdf.sail.nativerdf.model.NativeValue;

/**
 * A {@link ValueStore ValueStore} revision for {@link NativeValue NativeValue}
 * objects. For a cached value ID of a NativeValue to be valid, the revision
 * object needs to be equal to the concerning ValueStore's revision object. The
 * ValueStore's revision object is changed whenever values are removed from it
 * or IDs are changed.
 * 
 * @author Arjohn Kampman
 */
public class ValueStoreRevision implements Serializable {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * 
	 */
	private static final long serialVersionUID = -2434063125560285009L;
	
	transient private ValueStore valueStore;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ValueStoreRevision(ValueStore valueStore) {
		this.valueStore = valueStore;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public ValueStore getValueStore() {
		return valueStore;
	}
}
