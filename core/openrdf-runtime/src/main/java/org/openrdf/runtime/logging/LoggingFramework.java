/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.runtime.logging;

import java.util.logging.Handler;
import java.util.logging.LogManager;

/**
 * LoggingFramework provides a non-intrusive way for OpenRDF logging to be
 * handled by a specific logging framework. By default, OpenRDF classes use
 * java.util.logging. By registering an appropriate handler with the root
 * logger, logging can be transferred to another framework, such as Apache
 * log4j.
 * 
 * @author Herko ter Horst
 */
public enum LoggingFramework {
	/**
	 * Default logging framework is java.util.logging.
	 */
	DEFAULT(null),
	/**
	 * Apache log4j logging framework.
	 */
	LOG4J("org.openrdf.runtime.logging.Log4jHandler");

	private String handlerClassName;

	private Handler handler;

	/**
	 * Construct a new LoggingFramework instance
	 * 
	 * @param handlerClassName
	 *        the FQCN of the handler class for this LoggingFramework. The
	 *        handler will not be instantiated until this LoggingFramework is
	 *        enabled.
	 */
	private LoggingFramework(String handlerClassName) {
		this.handlerClassName = handlerClassName;
	}

	/**
	 * Enable the logging framework represented by this object to handle logging
	 * requests.
	 * 
	 * @see java.util.logging.LogManager.reset();
	 */
	public void enable() {
		// try to instantiate the handler if needed
		if (handler == null && handlerClassName != null) {
			try {
				handler = (Handler)Class.forName(handlerClassName).newInstance();
			}
			catch (InstantiationException e) {
				e.printStackTrace();
			}
			catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		// if there is a handler, register it with the root logger of the
		// java.util.logging framework
		if (handler != null) {
			LogManager.getLogManager().reset();
			LogManager.getLogManager().getLogger("").addHandler(handler);
		}
	}

	/**
	 * Disable the handler for this logging framework.
	 */
	public void disable() {
		// if there is a handler, remove it from the root logger of the
		// java.util.logging framework
		if (handler != null) {
			LogManager.getLogManager().getLogger("").removeHandler(handler);
			handler = null;
		}
	}
}
