/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.error;


/**
 *
 * @author Herko ter Horst
 */
public interface ExceptionInfo {
	
	public static final String EXCEPTION_CLASS_KEY = "exceptionClass";
	
	public static final String EXCEPTION_MESSAGE_KEY = "exceptionMessage";
	
	public static final String HAS_PARSE_INFO_KEY = "hasParseInfo";
	
	public String getExceptionClassName();
	
	public String getExceptionMessage();
	
	public boolean hasParseInfo();
	
	public ParseInfo getParseInfo();
	
	public void setParseInfo(ParseInfo parseInfo);
}
