/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory.model;

import org.openrdf.model.impl.ContextStatementImpl;

/**
 * A MemStatement is a Statement which contains context information and a flag
 * indicating whether the statement is explicit or inferred.
 */
public class MemStatement extends ContextStatementImpl {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * 
	 */
	private static final long serialVersionUID = -3073275483628334134L;

	/**
	 * Flag indicating whether or not this statement has been added explicitly or
	 * that it has been inferred.
	 */
	private boolean explicit;

	/**
	 * Flag indicating the status of of this statement during a transaction.
	 */
	private TxnStatus txnStatus;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new MemStatement with the supplied subject, predicate, object
	 * and context and marks it as 'explicit'.
	 */
	public MemStatement(MemResource subject, MemURI predicate, MemValue object, MemResource context) {
		this(subject, predicate, object, context, true);
	}

	/**
	 * Creates a new MemStatement with the supplied subject, predicate, object
	 * and context. The value of the <tt>explicit</tt> parameter determines if
	 * this statement is marked as 'explicit' or not.
	 */
	public MemStatement(MemResource subject, MemURI predicate, MemValue object, MemResource context,
			boolean explicit)
	{
		super(subject, predicate, object, context);
		this.explicit = explicit;
	}

	/*---------*
	 * Methods *
	 *---------*/

	// Overrides StatementImpl.getSubject()
	public MemResource getSubject() {
		return (MemResource)super.getSubject();
	}

	// Overrides StatementImpl.getPredicate()
	public MemURI getPredicate() {
		return (MemURI)super.getPredicate();
	}

	// Overrides StatementImpl.getObject()
	public MemValue getObject() {
		return (MemValue)super.getObject();
	}

	// Overrides ContextStatementImpl.getContext()
	public MemResource getContext() {
		return (MemResource)super.getContext();
	}

	public void setExplicit(boolean explicit) {
		this.explicit = explicit;
	}

	public boolean isExplicit() {
		return explicit;
	}

	public void setTxnStatus(TxnStatus txnStatus) {
		this.txnStatus = txnStatus;
	}

	public TxnStatus getTxnStatus() {
		return txnStatus;
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
		txnStatus = TxnStatus.NEW;
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
		txnStatus = null;
	}
}
