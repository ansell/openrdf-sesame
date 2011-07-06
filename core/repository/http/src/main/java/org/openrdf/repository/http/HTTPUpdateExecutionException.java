/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import java.io.IOException;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryException;


/**
 *
 * @author Jeen Broekstra
 */
public class HTTPUpdateExecutionException extends UpdateExecutionException {

	private static final long serialVersionUID = -8315025167877093273L;

	/**
	 * @param msg
	 */
	public HTTPUpdateExecutionException(String msg) {
		super(msg);
	}
	/**
	 * @param msg
	 * @param cause
	 */
	public HTTPUpdateExecutionException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * @param cause
	 */
	public HTTPUpdateExecutionException(Throwable cause) {
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
