/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.rio.ParseErrorListener;


/**
 * A ParseErrorListener that reports Rio parser errors to the SLf4J Logging framework.
 * 
 * @author jeen
 */
public class ParseErrorLogger implements ParseErrorListener {

	private final Logger logger = LoggerFactory.getLogger(ParseErrorLogger.class);
	
	public void warning(String msg, int lineNo, int colNo) {
		logger.warn(msg + " (" + lineNo + ", " + colNo + ")");
	}

	public void error(String msg, int lineNo, int colNo) {
		logger.warn("[Rio error] " + msg + " (" + lineNo + ", " + colNo + ")");
	}

	public void fatalError(String msg, int lineNo, int colNo) {
		logger.error("[Rio fatal] " + msg + " (" + lineNo + ", " + colNo + ")");
	}

}
