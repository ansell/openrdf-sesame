/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.transaction.operations;

import info.aduna.lang.ObjectUtil;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * A context operation with (optional) subject, predicate, object.
 * 
 * @author Arjohn Kampman
 * @author Leo Sauermann
 */
public abstract class StatementOperation extends ContextOperation {

	private Resource subject;

	private URI predicate;

	private Value object;

	protected StatementOperation(Resource... contexts) {
		super(contexts);
	}

	public Resource getSubject() {
		return subject;
	}

	public void setSubject(Resource subject) {
		this.subject = subject;
	}

	public URI getPredicate() {
		return predicate;
	}

	public void setPredicate(URI predicate) {
		this.predicate = predicate;
	}

	public Value getObject() {
		return object;
	}

	public void setObject(Value object) {
		this.object = object;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof StatementOperation) {
			StatementOperation o = (StatementOperation)other;

			return ObjectUtil.nullEquals(getSubject(), o.getSubject())
					&& ObjectUtil.nullEquals(getPredicate(), o.getPredicate())
					&& ObjectUtil.nullEquals(getObject(), o.getObject())
					&& super.equals(other);
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		int hashCode = ObjectUtil.nullHashCode(getSubject());
		hashCode = 31 * hashCode + ObjectUtil.nullHashCode(getPredicate());
		hashCode = 31 * hashCode + ObjectUtil.nullHashCode(getObject());
		hashCode = 31 * hashCode + super.hashCode();
		return hashCode;
	}
}
