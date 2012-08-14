/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.app.logging;

import info.aduna.app.AppConfiguration;
import info.aduna.app.config.Configuration;
import info.aduna.logging.LogReader;

import java.io.File;
import java.io.IOException;

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