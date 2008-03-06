/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.base;

import java.util.Set;

import org.openrdf.http.protocol.error.ParseInfo;

/**
 * 
 * @author Herko ter Horst
 */
public class ParseInfoBase implements ParseInfo {

	private String encounteredToken;

	private long line;

	private long position;

	private Set<String> expectedTokens;

	public ParseInfoBase() {
		this(null, -1, -1, null);
	}

	public ParseInfoBase(String encounteredToken, long line, long position, Set<String> expectedTokens) {
		this.encounteredToken = encounteredToken;
		this.line = line;
		this.position = position;
		this.expectedTokens = expectedTokens;
	}

	public String getEncounteredToken() {
		return encounteredToken;
	}

	public void setEncounteredToken(String encounteredToken) {
		this.encounteredToken = encounteredToken;
	}

	public long getLineNumber() {
		return line;
	}

	public void setLine(long line) {
		this.line = line;
	}

	public long getColumnNumber() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
	}

	public Set<String> getExpectedTokens() {
		return expectedTokens;
	}

	public void setExpectedTokens(Set<String> expectedTokens) {
		this.expectedTokens = expectedTokens;
	}
}
