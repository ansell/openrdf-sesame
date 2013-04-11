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
package info.aduna.app.logging.logback;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.app.logging.base.LogConfigurationBase;
import info.aduna.app.util.ConfigurationUtil;
import info.aduna.io.IOUtil;
import info.aduna.logging.LogReader;
import info.aduna.logging.file.logback.FileLogReader;

public class LogbackConfiguration extends LogConfigurationBase {

	public static final String LOGGING_DIR_PROPERTY = "info.aduna.logging.dir";

	private static final String LOGBACK_CONFIG_FILE = "logback.xml";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Path configFile;

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
		logger.debug("Log dir: {}", getLoggingDir().toAbsolutePath());

		save();
	}

	public void load()
		throws IOException
	{
		try {
			if (System.getProperty(LOGGING_DIR_PROPERTY) == null) {
				System.setProperty(LOGGING_DIR_PROPERTY, getLoggingDir().toAbsolutePath().toString());
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
			configurator.doConfigure(configFile.toFile());
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

	private Path getConfigFile()
		throws IOException
	{
		Path f = getConfDir().resolve(LOGBACK_CONFIG_FILE);
		if (!Files.exists(f)) { // || !Files.isReadable(f)) {
			String content = ConfigurationUtil.loadConfigurationContents(LOGBACK_CONFIG_FILE);
			content = content.replace("${logging.main.file}", LOG_FILE);
			content = content.replace("${logging.event.user.file}", USER_EVENT_LOG_FILE);
			content = content.replace("${logging.event.admin.file}", ADMIN_EVENT_LOG_FILE);
			content = content.replace("${logging.event.user.logger}", USER_EVENT_LOGGER_NAME);
			content = content.replace("${logging.event.admin.logger}", ADMIN_EVENT_LOGGER_NAME);
			Files.createDirectories(f.getParent());

			if (!Files.isWritable(f.getParent())) {
				throw new IOException("Not allowed to write logging configuration file to " + f.getParent());
			}
			else {
				f = Files.createFile(f);

				Files.write(f, content.getBytes());
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
		return new FileLogReader(getLoggingDir().resolve(LOG_FILE).toFile());
	}
}
