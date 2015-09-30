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
package org.eclipse.rdf4j.sail.nativerdf.model;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleLiteral;
import org.eclipse.rdf4j.sail.nativerdf.ValueStoreRevision;

public class NativeLiteral extends SimpleLiteral implements NativeValue {

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

	public NativeLiteral(ValueStoreRevision revision, String label, IRI datatype) {
		this(revision, label, datatype, UNKNOWN_ID);
	}

	public NativeLiteral(ValueStoreRevision revision, String label, IRI datatype, int internalID) {
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
