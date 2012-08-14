/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.model;

import org.openrdf.model.impl.ContextStatementImpl;

/**
 * Rdbms typed statement.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsStatement extends ContextStatementImpl {

	private static final long serialVersionUID = -5970166748706214658L;

	public RdbmsStatement(RdbmsResource subject, RdbmsURI predicate, RdbmsValue object) {
		this(subject, predicate, object, null);
	}

	public RdbmsStatement(RdbmsResource subject, RdbmsURI predicate, RdbmsValue object, RdbmsResource context)
	{
		super(subject, predicate, object, context);
	}

	@Override
	public RdbmsResource getSubject() {
		return (RdbmsResource)super.getSubject();
	}

	@Override
	public RdbmsURI getPredicate() {
		return (RdbmsURI)super.getPredicate();
	}

	@Override
	public RdbmsValue getObject() {
		return (RdbmsValue)super.getObject();
	}

	@Override
	public RdbmsResource getContext() {
		return (RdbmsResource)super.getContext();
	}

}
