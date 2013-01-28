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

	private volatile ValueStoreRevision revision;

	private volatile int internalID;

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
				// NativeBNode's from the same revision of the same native store,
				// with both ID's set
				return internalID == otherNativeBNode.internalID;
			}
		}

		return super.equals(o);
	}

}
