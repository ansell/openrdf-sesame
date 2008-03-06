/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.error;

import java.util.Set;


/**
 *
 * @author Herko ter Horst
 */
public interface ParseInfo {
	
	public static final String ENCOUNTERD_TOKEN_KEY = "parse.encounteredToken";
	
	public static final String LINE_KEY = "parse.lineNumber";
	
	public static final String COLUMN_KEY = "parse.columnNumber";
	
	public static final String EXPECTED_TOKEN_COUNT_KEY = "parse.expectedTokenCount";
	
	public static final String EXPECTED_TOKEN_KEY_PREFIX = "parse.expectedToken";
	
	public String getEncounteredToken();
	
	public long getLineNumber();
	
	public long getColumnNumber();
	
	public Set<String> getExpectedTokens();
}
