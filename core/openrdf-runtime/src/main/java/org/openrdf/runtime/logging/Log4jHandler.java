/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.runtime.logging;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.apache.log4j.Logger;

/**
 * Log4jHandler is a handler for the java.util.logging framework that delegates
 * logging to an Apache log4j logger.
 * 
 * @author Herko ter Horst
 */
public class Log4jHandler extends Handler {

	@Override
	public void publish(LogRecord record)
	{
		Logger logger = Logger.getLogger(record.getLoggerName());
		Level level = record.getLevel();
		String message = record.getMessage();
		Throwable throwable = record.getThrown();
		if (Level.SEVERE.equals(level)) {
			logger.error(message, throwable);
		}
		else if (Level.WARNING.equals(level)) {
			logger.warn(message, throwable);
		}
		else if (Level.INFO.equals(level) || Level.CONFIG.equals(level)) {
			logger.info(message, throwable);
		}
		else {
			logger.debug(message, throwable);
		}
	}

	@Override
	public void close()
	{
		// nop
	}

	@Override
	public void flush()
	{
		// nop
	}
}