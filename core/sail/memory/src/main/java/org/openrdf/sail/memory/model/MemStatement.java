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
package org.openrdf.sail.memory.model;

import org.openrdf.model.impl.ContextStatement;

/**
 * A MemStatement is a Statement which contains context information and a flag
 * indicating whether the statement is explicit or inferred.
 */
public class MemStatement extends ContextStatement {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final long serialVersionUID = -3073275483628334134L;

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * Flag indicating whether or not this statement has been added explicitly or
	 * that it has been inferred.
	 */
	private volatile boolean explicit;

	/**
	 * Identifies the snapshot in which this statement was introduced.
	 */
	private volatile int sinceSnapshot;

	/**
	 * Identifies the snapshot in which this statement was revoked, defaults to
	 * {@link Integer#MAX_VALUE}.
	 */
	private volatile int tillSnapshot = Integer.MAX_VALUE;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new MemStatement with the supplied subject, predicate, object
	 * and context and marks it as 'explicit'.
	 */
	public MemStatement(MemResource subject, MemIRI predicate, MemValue object, MemResource context,
			int sinceSnapshot)
	{
		this(subject, predicate, object, context, true, sinceSnapshot);
	}

	/**
	 * Creates a new MemStatement with the supplied subject, predicate, object
	 * and context. The value of the <tt>explicit</tt> parameter determines if
	 * this statement is marked as 'explicit' or not.
	 */
	public MemStatement(MemResource subject, MemIRI predicate, MemValue object, MemResource context,
			boolean explicit, int sinceSnapshot)
	{
		super(subject, predicate, object, context);
		setExplicit(explicit);
		setSinceSnapshot(sinceSnapshot);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public MemResource getSubject() {
		return (MemResource)super.getSubject();
	}

	@Override
	public MemIRI getPredicate() {
		return (MemIRI)super.getPredicate();
	}

	@Override
	public MemValue getObject() {
		return (MemValue)super.getObject();
	}

	@Override
	public MemResource getContext() {
		return (MemResource)super.getContext();
	}

	public void setSinceSnapshot(int snapshot) {
		sinceSnapshot = snapshot;
	}

	public int getSinceSnapshot() {
		return sinceSnapshot;
	}

	public void setTillSnapshot(int snapshot) {
		tillSnapshot = snapshot;
	}

	public int getTillSnapshot() {
		return tillSnapshot;
	}

	public boolean isInSnapshot(int snapshot) {
		return snapshot >= sinceSnapshot && snapshot < tillSnapshot;
	}

	public void setExplicit(boolean explicit) {
		this.explicit = explicit;
	}

	public boolean isExplicit() {
		return explicit;
	}

	/**
	 * Lets this statement add itself to the appropriate statement lists of its
	 * subject, predicate, object and context. The transaction status will be set
	 * to {@link TxnStatus#NEW}.
	 */
	public void addToComponentLists() {
		getSubject().addSubjectStatement(this);
		getPredicate().addPredicateStatement(this);
		getObject().addObjectStatement(this);
		MemResource context = getContext();
		if (context != null) {
			context.addContextStatement(this);
		}
	}

	/**
	 * Lets this statement remove itself from the appropriate statement lists of
	 * its subject, predicate, object and context. The transaction status will be
	 * set to <tt>null</tt>.
	 */
	public void removeFromComponentLists() {
		getSubject().removeSubjectStatement(this);
		getPredicate().removePredicateStatement(this);
		getObject().removeObjectStatement(this);
		MemResource context = getContext();
		if (context != null) {
			context.removeContextStatement(this);
		}
	}
}
