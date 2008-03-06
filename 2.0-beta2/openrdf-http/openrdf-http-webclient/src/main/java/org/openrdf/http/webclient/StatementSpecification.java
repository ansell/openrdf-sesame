/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * @author Herko ter Horst
 */
public class StatementSpecification {

	private Resource subject = null;

	private URI predicate = null;

	private Value object = null;
	
	boolean includeInferred = false;

	private Resource[] contexts = null;

	public Resource[] getContexts() {
		return contexts;
	}

	/**
	 * @return Returns the predicate.
	 */
	public URI getPredicate() {
		return predicate;
	}

	/**
	 * @param predicate
	 *        The predicate to set.
	 */
	public void setPredicate(URI predicate) {
		this.predicate = predicate;
	}

	/**
	 * @return Returns the subject.
	 */
	public Resource getSubject() {
		return subject;
	}

	/**
	 * @param subject
	 *        The subject to set.
	 */
	public void setSubject(Resource subject) {
		this.subject = subject;
	}

	/**
	 * @return Returns the value.
	 */
	public Value getObject() {
		return object;
	}

	/**
	 * @param value
	 *        The value to set.
	 */
	public void setObject(Value object) {
		this.object = object;
	}

	/**
	 * @param contexts
	 *        The contexts to set.
	 */
	public void setContexts(Resource[] contexts) {
		this.contexts = contexts;
	}

	
	/**
	 * @return Returns the includeInferred.
	 */
	public boolean isIncludeInferred() {
		return includeInferred;
	}

	
	/**
	 * @param includeInferred The includeInferred to set.
	 */
	public void setIncludeInferred(boolean includeInferred) {
		this.includeInferred = includeInferred;
	}
}
