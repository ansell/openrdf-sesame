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
package org.eclipse.rdf4j.common.app.logging.logback;

import java.io.File;
import java.io.IOException;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

import org.eclipse.rdf4j.common.app.logging.base.AbstractLogConfiguration;
import org.eclipse.rdf4j.common.app.util.ConfigurationUtil;
import org.eclipse.rdf4j.common.io.IOUtil;
import org.eclipse.rdf4j.common.logging.LogReader;
import org.eclipse.rdf4j.common.logging.file.logback.FileLogReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogbackConfiguration extends AbstractLogConfiguration {

	public static final String LOGGING_DIR_PROPERTY = "info.aduna.logging.dir";

	private static final String LOGBACK_CONFIG_FILE = "logback.xml";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private File configFile;

	private LogConfigurator configurator = null;

	public LogbackConfiguration()
		throws IOException
	{
		super();
		// USE init() FOR FURTHER CONFIGURATION
		// it will be called from the super constructor
	}

	public void init()
		throws IOException
	{
		configFile = getConfigFile();

		load();

		logger.info("Logback logging API implementation is configured.");
		logger.debug("Log dir: {}", getLoggingDir().getAbsolutePath());

		save();
	}

	public void load()
		throws IOException
	{
		try {
			if (System.getProperty(LOGGING_DIR_PROPERTY) == null) {
				System.setProperty(LOGGING_DIR_PROPERTY, getLoggingDir().getAbsolutePath());
			}
		}
		catch (SecurityException e) {
			System.out.println("Not allowed to read or write system property '" + LOGGING_DIR_PROPERTY + "'");
		}

		LoggerContext lc = (LoggerContext)LoggerFactory.getILoggerFactory();
		try {
			configurator = new LogConfigurator();
			configurator.setContext(lc);
			lc.reset();
			configurator.doConfigure(configFile);
		}
		catch (JoranException je) {
			System.out.println("Logback configuration error");
			je.printStackTrace();
			StatusPrinter.print(lc);
		}
	}

	public void save()
		throws IOException
	{
		// nop
	}

	public void destroy() {
		// look up all loggers in the logger context and close
		// all appenders configured for them.
		LoggerContext lc = (LoggerContext)LoggerFactory.getILoggerFactory();
		lc.reset();
	}

	private File getConfigFile()
		throws IOException
	{
		File f = new File(getConfDir(), LOGBACK_CONFIG_FILE);
		if (!f.exists() || !f.canRead()) {
			String content = ConfigurationUtil.loadConfigurationContents(LOGBACK_CONFIG_FILE);
			content = content.replace("${logging.main.file}", LOG_FILE);
			content = content.replace("${logging.event.user.file}", USER_EVENT_LOG_FILE);
			content = content.replace("${logging.event.admin.file}", ADMIN_EVENT_LOG_FILE);
			content = content.replace("${logging.event.user.logger}", USER_EVENT_LOGGER_NAME);
			content = content.replace("${logging.event.admin.logger}", ADMIN_EVENT_LOGGER_NAME);
			if (!f.getParentFile().mkdirs() && !f.getParentFile().canWrite()) {
				throw new IOException("Not allowed to write logging configuration file to " + f.getParent());
			}
			else {
				IOUtil.writeString(content, f);
			}
		}
		return f;
	}

	public LogReader getLogReader(String appender) {
		return this.configurator.getLogReader(appender);
	}

	public LogReader getDefaultLogReader() {
		LogReader logReader = this.configurator.getDefaultLogReader();
		if (logReader != null) {
			return logReader;
		}
		return new FileLogReader(new File(getLoggingDir(), LOG_FILE));
	}
}
