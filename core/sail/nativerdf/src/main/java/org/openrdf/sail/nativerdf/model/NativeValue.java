/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
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
