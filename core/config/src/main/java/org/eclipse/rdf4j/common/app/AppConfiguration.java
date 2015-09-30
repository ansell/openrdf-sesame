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
package org.eclipse.rdf4j.common.app;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.rdf4j.Sesame;
import org.eclipse.rdf4j.common.app.config.Configuration;
import org.eclipse.rdf4j.common.app.logging.LogConfiguration;
import org.eclipse.rdf4j.common.app.net.ProxySettings;
import org.eclipse.rdf4j.common.app.util.ConfigurationUtil;
import org.eclipse.rdf4j.common.io.MavenUtil;
import org.eclipse.rdf4j.common.platform.PlatformFactory;

/**
 * @author Herko ter Horst
 */
public class AppConfiguration implements Configuration {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final String APP_CONFIG_FILE = "application.properties";

	private static final String DEFAULT_PREFIX = "Aduna";

	private static final String DEFAULT_LOGGING = "info.aduna.app.logging.logback.LogbackConfiguration";

	/*-----------*
	 * Variables *
	 *-----------*/

	private String applicationId;

	private String longName;

	private String fullName;

	private AppVersion version;

	private String[] commandLineArgs;

	private String dataDirName;

	private File dataDir;

	private LogConfiguration loggingConfiguration;

	private ProxySettings proxySettings;

	private Properties properties;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Create a new, uninitialized application configuration.
	 */
	public AppConfiguration() {
		super();
	}

	/**
	 * Create the application configuration.
	 * 
	 * @param applicationId
	 *        the ID of the application
	 */
	public AppConfiguration(final String applicationId) {
		this();
		setApplicationId(applicationId);
	}

	/**
	 * Create the application configuration.
	 * 
	 * @param applicationId
	 *        the ID of the application
	 * @param version
	 *        the application's version
	 */
	public AppConfiguration(final String applicationId, final AppVersion version) {
		this(applicationId);
		setVersion(version);
	}

	/**
	 * Create the application configuration.
	 * 
	 * @param applicationId
	 *        the ID of the application
	 * @param longName
	 *        the long name of the application
	 */
	public AppConfiguration(final String applicationId, final String longName) {
		this(applicationId);
		setLongName(longName);
	}
	
	/**
	 * Create the application configuration.
	 * 
	 * @param applicationId
	 *        the ID of the application
	 * @param longName
	 *        the long name of the application
	 * @param version
	 *        the application's version
	 */
	public AppConfiguration(final String applicationId, final String longName, final AppVersion version) {
		this(applicationId, version);
		setLongName(longName);
	}

	/*---------*
	 * Methods *
	 ----------*/

	public void load()
		throws IOException
	{
		properties = ConfigurationUtil.loadConfigurationProperties(APP_CONFIG_FILE, null);
	}

	public void save()
		throws IOException
	{
		if (null != loggingConfiguration) {
			loggingConfiguration.save();
		}
		proxySettings.save();
	}

	public void init()
		throws IOException
	{
		this.init(true);
	}

	public void init(final boolean loadLogConfig)
		throws IOException
	{
		if (longName == null) {
			setLongName(DEFAULT_PREFIX + " " + applicationId);
		}
		setFullName();
		configureDataDir();
		load();
		if (loadLogConfig) {
			try {
				loggingConfiguration = loadLogConfiguration();
				loggingConfiguration.setBaseDir(getDataDir());
				loggingConfiguration.setAppConfiguration(this);
				loggingConfiguration.init();
			}
			catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			catch (InstantiationException e) {
				e.printStackTrace();
			}
			catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		proxySettings = new ProxySettings(getDataDir());
		proxySettings.init();
		save();
	}

	public void destroy()
		throws IOException
	{
		loggingConfiguration.destroy();
		// proxySettings.destroy();
	}

	/**
	 * Get the name of the application (e.g. "AutoFocus" or "Metadata Server").
	 * 
	 * @return the name of the application
	 */
	public String getApplicationId() {
		return applicationId;
	}

	public final void setApplicationId(final String applicationId) {
		this.applicationId = applicationId;
	}

	public void setDataDirName(final String dataDirName) {
		this.dataDirName = dataDirName;
	}

	/**
	 * Get the long name of the application (e.g. "Aduna AutoFocus" or "OpenRDF
	 * Sesame Server").
	 * 
	 * @return the long name of the application
	 */
	public String getLongName() {
		return longName;
	}

	/**
	 * Set the long name of the application.
	 * 
	 * @param longName
	 *        the new name
	 */
	public final void setLongName(final String longName) {
		this.longName = longName;
	}

	/**
	 * Get the full name of the application, which consists of the long name and
	 * the version number (e.g. "Aduna AutoFocus 4.0-beta1" or "OpenRDF Sesame
	 * Webclient 2.0")
	 * 
	 * @return the full name of the application
	 */
	public String getFullName() {
		return fullName;
	}

	private void setFullName() {
		this.fullName = longName;
		if (version != null) {
			fullName = fullName + " " + version.toString();
		}
	}

	/**
	 * Get the version of the application.
	 * 
	 * @return the version of the application
	 */
	public AppVersion getVersion() {
		if (version == null) {
			version = AppVersion.parse(Sesame.getVersion());
		}
		return version;
	}

	/**
	 * Set the version of the application.
	 * 
	 * @param version
	 *        the new version
	 */
	public final void setVersion(final AppVersion version) {
		this.version = version;
		this.fullName = longName + " " + version.toString();
	}

	/**
	 * Get the command line arguments of the application.
	 * 
	 * @return A String array, as (typically) specified to the main method.
	 */
	public String[] getCommandLineArgs() {
		return (String[])commandLineArgs.clone();
	}

	/**
	 * Set the command line arguments specified to the application.
	 * 
	 * @param args
	 *        A String array containing the arguments as specified to the main
	 *        method.
	 */
	public void setCommandLineArgs(final String[] args) {
		this.commandLineArgs = (String[])args.clone();
	}

	public File getDataDir() {
		return dataDir;
	}

	public LogConfiguration getLogConfiguration() {
		return loggingConfiguration;
	}

	public ProxySettings getProxySettings() {
		return proxySettings;
	}

	public void setProxySettings(final ProxySettings proxySettings) {
		this.proxySettings = proxySettings;
	}

	/**
	 * Configure the data dir.
	 * 
	 * @param dataDirParam
	 *        the data dir to use. If null, determination of the data dir will be
	 *        deferred to Platform.
	 */
	private void configureDataDir() {
		if (dataDirName != null) {
			dataDirName = dataDirName.trim();
			if (!("".equals(dataDirName))) {
				final File dataDirCandidate = new File(dataDirName);
				dataDirCandidate.mkdirs();
				// change data directory if the previous code was successful
				dataDir = (dataDirCandidate.canRead() && dataDirCandidate.canWrite()) ? dataDirCandidate
						: dataDir;
			}
		}
		if (dataDir == null) {
			dataDir = PlatformFactory.getPlatform().getApplicationDataDir(applicationId);
		}
	}

	/**
	 * Load and instantiate the logging configuration.
	 * 
	 * @return the logging configuration
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private LogConfiguration loadLogConfiguration()
		throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		String classname = this.properties.getProperty("feature.logging.impl");
		if (classname == null) {
			classname = DEFAULT_LOGGING;
		}
		final Class<?> logImplClass = Class.forName(classname);
		final Object logImpl = logImplClass.newInstance();
		if (logImpl instanceof LogConfiguration) {
			return (LogConfiguration)logImpl;
		}
		throw new InstantiationException(classname + " is not valid LogConfiguration instance!");
	}

	/**
	 * @return Returns the properties.
	 */
	public Properties getProperties() {
		return properties;
	}
}
