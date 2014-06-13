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

import java.util.Optional;

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

	private volatile ValueStoreRevision revision;

	private volatile int internalID;

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

	public NativeLiteral(ValueStoreRevision revision, String label, Optional<String> lang) {
		this(revision, label, lang, UNKNOWN_ID);
	}

	public NativeLiteral(ValueStoreRevision revision, String label, Optional<String> lang, int internalID) {
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
