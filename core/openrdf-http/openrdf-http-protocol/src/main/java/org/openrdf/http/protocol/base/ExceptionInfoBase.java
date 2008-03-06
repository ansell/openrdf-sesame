/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.base;

import org.openrdf.http.protocol.error.ExceptionInfo;
import org.openrdf.http.protocol.error.ParseInfo;


/**
 *
 * @author Herko ter Horst
 */
public class ExceptionInfoBase implements ExceptionInfo {

	private String exceptionClassName;
	private String exceptionMessage;
	private ParseInfo parseInfo;

	public ExceptionInfoBase(Throwable throwable) {
		this(throwable.getClass().getCanonicalName(), throwable.getMessage());
	}

	public ExceptionInfoBase(Throwable throwable, String exceptionMessage) {
		this(throwable.getClass().getCanonicalName(), exceptionMessage);
	}

	public ExceptionInfoBase(String exceptionClassName, String exceptionMessage) {
		this.exceptionClassName = exceptionClassName;
		this.exceptionMessage = exceptionMessage;
	}
	
	public String getExceptionClassName() {
		return exceptionClassName;
	}

	public String getExceptionMessage() {
		return exceptionMessage;
	}

	public void setParseInfo(ParseInfo parseInfo) {
		this.parseInfo = parseInfo;
	}
	
	public ParseInfo getParseInfo() {
		return parseInfo;
	}

	public boolean hasParseInfo() {
		return parseInfo != null;
	}

}
