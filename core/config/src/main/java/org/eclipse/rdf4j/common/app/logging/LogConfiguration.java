/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.common.app.logging;

import java.io.File;
import java.io.IOException;

import org.eclipse.rdf4j.common.app.AppConfiguration;
import org.eclipse.rdf4j.common.app.config.Configuration;
import org.eclipse.rdf4j.common.logging.LogReader;

/**
 * Configuration settings for application logging.
 * 
 * @author Herko ter Horst
 */
public interface LogConfiguration extends Configuration {

	public static final String LOGGING_DIR = "logs";

	public static final String LOG_FILE = "main.log";
	
	public static final String USER_EVENT_LOG_FILE = "user-event.log";
	
	public static final String ADMIN_EVENT_LOG_FILE = "admin-event.log";
	
	public static final String USER_EVENT_LOGGER_NAME = "event.user";
	
	public static final String ADMIN_EVENT_LOGGER_NAME = "event.admin";

	/**
	 * Set the base location on the file system for logging configuration and data
	 *
	 * @param baseDir the base location on the file system for logging configuration and data
	 * @throws IOException 
	 */
	public abstract void setBaseDir(File baseDir) throws IOException;
	
	/**
	 * The base location on the file system for logging configuration and data
	 * 
	 * @return the base location on the file system for logging configuration and data
	 */
	public abstract File getBaseDir();
	

	/**
	 * The location on the file system where logging configuration is stored.
	 * 
	 * @return the location on the file system where logging configuration is stored
	 */
	public abstract File getConfDir();

	/**
	 * The location on the file system where logging data is stored.
	 * 
	 * @return the location on the file system where logging data is stored
	 */
	public abstract File getLoggingDir();

	/**
	 * A reader that can read logging information as stored by the specific
	 * logger's appender.
	 * 
	 * @param appender Name of the appender to which the LogReader is attached
	 * 
	 * @return a reader that can read logging information as stored by the logger
	 *         configured through this LogConfiguration
	 */	
	public abstract LogReader getLogReader(String appender);
	
	/**
	 * Default (fallback) LogReader instance.
	 * 
	 * @return  default (fallback) LogReader instance.
	 */
	public abstract LogReader getDefaultLogReader();

	/**
	 * Is debug logging enabled?
	 * 
	 * @return true if debug logging is enabled, false otherwise
	 */
	public abstract boolean isDebugLoggingEnabled();

	/**
	 * Enable or disable debug logging.
	 * 
	 * @param enabled
	 *        set to true if debug logging should be enabled, set to false
	 *        otherwise
	 */
	public abstract void setDebugLoggingEnabled(boolean enabled);
	
	public abstract void setAppConfiguration(AppConfiguration config);
	
	public abstract AppConfiguration getAppConfiguration();

}
