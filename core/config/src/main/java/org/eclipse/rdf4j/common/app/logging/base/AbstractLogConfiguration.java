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
package org.eclipse.rdf4j.common.app.logging.base;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.rdf4j.common.app.AppConfiguration;
import org.eclipse.rdf4j.common.app.logging.LogConfiguration;
import org.eclipse.rdf4j.common.app.util.ConfigurationUtil;

/**
 * Base implementation of LogConfiguration.
 * 
 * @author Herko ter Horst
 */
public abstract class AbstractLogConfiguration implements LogConfiguration {

	private static final String LOGGING_CONFIG_FILE = "logging.properties";

	private static final String PACKAGES_SLF4J_KEY = "packages.slf4j";

	private static final String PACKAGES_JUL_KEY = "packages.jul";

	private File baseDir;
	
	private File confDir;

	private File loggingDir;

	private boolean debugLoggingEnabled;

	private Set<String> packages;
	
	private AppConfiguration config;

	protected AbstractLogConfiguration()
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
