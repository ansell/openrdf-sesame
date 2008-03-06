/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import java.util.Set;

import org.openrdf.OpenRDFException;

/**
 * An exception indicating that a query could not be processed by the query
 * parser, typically due to syntax errors.
 * 
 * @author jeen
 * @author Herko ter Horst
 */
public class MalformedQueryException extends OpenRDFException {

	private static final long serialVersionUID = 1210214405486786142L;

	private String encounteredToken;

	private long lineNumber;

	private long columnNumber;

	private Set<String> expectedTokens;

	public MalformedQueryException() {
		super();
	}

	public MalformedQueryException(String message) {
		super(message);
	}

	public MalformedQueryException(Throwable t) {
		super(t);
	}

	public MalformedQueryException(String message, Throwable t) {
		super(message, t);
	}

	public void setEncounteredToken(String encounteredToken) {
		this.encounteredToken = encounteredToken;
	}

	public String getEncounteredToken() {
		return encounteredToken;
	}

	public void setLineNumber(long lineNumber) {
		this.lineNumber = lineNumber;
	}

	public long getLineNumber() {
		return lineNumber;
	}

	public void setColumnNumber(long columnNumber) {
		this.columnNumber = columnNumber;
	}

	public long getColumnNumber() {
		return columnNumber;
	}

	public void setExpectedTokens(Set<String> expectedTokens) {
		this.expectedTokens = expectedTokens;
	}

	public Set<String> getExpectedTokens() {
		return expectedTokens;
	}
}
