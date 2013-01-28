/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
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
