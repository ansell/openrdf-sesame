/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import java.io.IOException;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;


/**
 *
 * @author Herko ter Horst
 */
public class HTTPQueryEvaluationException extends QueryEvaluationException {

	private static final long serialVersionUID = -8315025167877093272L;

	/**
	 * @param msg
	 */
	public HTTPQueryEvaluationException(String msg) {
		super(msg);
	}
	/**
	 * @param msg
	 * @param cause
	 */
	public HTTPQueryEvaluationException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * @param cause
	 */
	public HTTPQueryEvaluationException(Throwable cause) {
		super(cause);
	}

	public boolean isCausedByIOException() {
		return getCause() instanceof IOException;
	}
	
	public boolean isCausedByRepositoryException() {
		return getCause() instanceof RepositoryException;
	}
	
	public boolean isCausedByMalformedQueryException() {
		return getCause() instanceof MalformedQueryException;
	}

	public IOException getCauseAsIOException() {
		return (IOException)getCause();
	}

	public RepositoryException getCauseAsRepositoryException() {
		return (RepositoryException)getCause();
	}

	public MalformedQueryException getCauseAsMalformedQueryException() {
		return (MalformedQueryException)getCause();
	}
}
