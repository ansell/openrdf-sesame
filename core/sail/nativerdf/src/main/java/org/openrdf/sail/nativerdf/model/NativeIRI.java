/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.openrdf.sail.nativerdf.model;

import org.openrdf.model.impl.SimpleIRI;
import org.openrdf.sail.nativerdf.ValueStoreRevision;

public class NativeIRI extends SimpleIRI implements NativeResource {

	private static final long serialVersionUID = -5888138591826143179L;

	/*-----------*
	 * Constants *
	 *-----------*/

	private volatile ValueStoreRevision revision;

	private volatile int internalID;

	/*--------------*
	 * Constructors *
	 *--------------*/

	protected NativeIRI(ValueStoreRevision revision, int internalID) {
		super();
		setInternalID(internalID, revision);
	}

	public NativeIRI(ValueStoreRevision revision, String uri) {
		this(revision, uri, UNKNOWN_ID);
	}

	public NativeIRI(ValueStoreRevision revision, String uri, int internalID) {
		super(uri);
		setInternalID(internalID, revision);
	}

	public NativeIRI(ValueStoreRevision revision, String namespace, String localname) {
		this(revision, namespace + localname);
	}

	public NativeIRI(ValueStoreRevision revision, String namespace, String localname, int internalID) {
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

		if (o instanceof NativeIRI && internalID != NativeValue.UNKNOWN_ID) {
			NativeIRI otherNativeURI = (NativeIRI)o;

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
