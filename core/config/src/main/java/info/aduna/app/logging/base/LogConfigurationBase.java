/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.app.logging.base;

import info.aduna.app.AppConfiguration;
import info.aduna.app.logging.LogConfiguration;
import info.aduna.app.util.ConfigurationUtil;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base implementation of LogConfiguration.
 * 
 * @author Herko ter Horst
 */
public abstract class LogConfigurationBase implements LogConfiguration {

	private static final String LOGGING_CONFIG_FILE = "logging.properties";

	private static final String PACKAGES_SLF4J_KEY = "packages.slf4j";

	private static final String PACKAGES_JUL_KEY = "packages.jul";

	private File baseDir;
	
	private File confDir;

	private File loggingDir;

	private boolean debugLoggingEnabled;

	private Set<String> packages;
	
	private AppConfiguration config;

	protected LogConfigurationBase()
		throws IOException
	{		
		debugLoggingEnabled = false;
		packages = new LinkedHashSet<String>();
		initBase();
	}
	
	public void setBaseDir(File baseDir) throws IOException {
		this.baseDir = baseDir;
		confDir = new File(baseDir, DIR);
		loggingDir = new File(baseDir, LOGGING_DIR);
		if (!loggingDir.mkdirs() && !loggingDir.canWrite()) {
			throw new IOException("Unable to create logging directory " + loggingDir.getAbsolutePath());
		}
	}
	
	public File getBaseDir() {
		return this.baseDir;
	}

	public File getConfDir() {
		return confDir;
	}

	public File getLoggingDir() {
		return loggingDir;
	}

	private void initBase()
		throws IOException
	{
		Properties loggingConfig = ConfigurationUtil.loadConfigurationProperties(LOGGING_CONFIG_FILE, null);

		String slf4jPackages = loggingConfig.getProperty(PACKAGES_SLF4J_KEY);

		if (slf4jPackages != null) {
			String[] slf4jPackageNames = slf4jPackages.split(",");

			for (String packageName : slf4jPackageNames) {
				packages.add(packageName);
			}
		}

		String julPackages = loggingConfig.getProperty(PACKAGES_JUL_KEY);

		if (julPackages != null) {
			String[] julPackageNames = julPackages.split(",");

			for (String packageName : julPackageNames) {
				packages.add(packageName);

				Logger logger = Logger.getLogger(packageName.trim());
				logger.setUseParentHandlers(false);
				logger.setLevel(Level.ALL);
				logger.addHandler(new LogConverterHandler());
			}
		}
	}

	public boolean isDebugLoggingEnabled() {
		return debugLoggingEnabled;
	}

	public void setDebugLoggingEnabled(boolean debugLoggingEnabled) {
		this.debugLoggingEnabled = debugLoggingEnabled;
	}

	protected Set<String> getPackages() {
		return Collections.unmodifiableSet(packages);
	}
	
	public AppConfiguration getAppConfiguration() {
		return this.config;
	}

	public void setAppConfiguration(AppConfiguration config) {
		this.config = config;
	}
}
