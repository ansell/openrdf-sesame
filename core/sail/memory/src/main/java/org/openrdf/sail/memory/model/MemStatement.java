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
package org.openrdf.sail.memory.model;

import org.openrdf.model.impl.ContextStatementImpl;

/**
 * A MemStatement is a Statement which contains context information and a flag
 * indicating whether the statement is explicit or inferred.
 */
public class MemStatement extends ContextStatementImpl {

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
	public MemStatement(MemResource subject, MemURI predicate, MemValue object, MemResource context,
			int sinceSnapshot)
	{
		this(subject, predicate, object, context, true, sinceSnapshot);
	}

	/**
	 * Creates a new MemStatement with the supplied subject, predicate, object
	 * and context. The value of the <tt>explicit</tt> parameter determines if
	 * this statement is marked as 'explicit' or not.
	 */
	public MemStatement(MemResource subject, MemURI predicate, MemValue object, MemResource context,
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
	public MemURI getPredicate() {
		return (MemURI)super.getPredicate();
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
