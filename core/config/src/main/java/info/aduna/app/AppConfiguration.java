/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.app;

import info.aduna.app.config.Configuration;
import info.aduna.app.logging.LogConfiguration;
import info.aduna.app.net.ProxySettings;
import info.aduna.app.util.ConfigurationUtil;
import info.aduna.platform.PlatformFactory;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Herko ter Horst
 */
public class AppConfiguration implements Configuration {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final String APP_CONFIG_FILE = "application.properties";
	private static final String DEFAULT_LONGNAME_PREFIX = "Aduna";
	private static final String DEFAULT_LOGGING_IMPL = "info.aduna.app.logging.logback.LogbackConfiguration";

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
	}

	/**
	 * Create the application configuration.
	 * 
	 * @param applicationId
	 *        the ID of the application
	 */
	public AppConfiguration(String applicationId) {
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
	public AppConfiguration(String applicationId, AppVersion version) {
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
	 * @param version
	 *        the application's version
	 */
	public AppConfiguration(String applicationId, String longName, AppVersion version) {
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
		loggingConfiguration.save();
		proxySettings.save();
	}

	public void init()
		throws IOException
	{
		if (longName == null) {
			setLongName(DEFAULT_LONGNAME_PREFIX + " " + applicationId);
		}
		setFullName();

		configureDataDir();

		load();

		try {
			loggingConfiguration = loadLogConfiguration();
			loggingConfiguration.setBaseDir(getDataDir());
			loggingConfiguration.setAppConfiguration(this);
			loggingConfiguration.init();
		}
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public void setDataDirName(String dataDirName) {
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
	public void setLongName(String longName) {
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
		return version;
	}

	/**
	 * Set the version of the application.
	 * 
	 * @param version
	 *        the new version
	 */
	public void setVersion(AppVersion version) {
		this.version = version;
		this.fullName = longName + " " + version.toString();
	}

	/**
	 * Get the command line arguments of the application.
	 * 
	 * @return A String array, as (typically) specified to the main method.
	 */
	public String[] getCommandLineArgs() {
		return commandLineArgs;
	}

	/**
	 * Set the command line arguments specified to the application.
	 * 
	 * @param args
	 *        A String array containing the arguments as specified to the main
	 *        method.
	 */
	public void setCommandLineArgs(String[] args) {
		this.commandLineArgs = args;
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

	public void setProxySettings(ProxySettings proxySettings) {
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

			if (!(dataDirName.equals(""))) {
				File dataDirCandidate = new File(dataDirName);
				dataDirCandidate.mkdirs();

				// set this datadir if the previous code was succesful
				if (dataDirCandidate.canRead() && dataDirCandidate.canWrite()) {
					dataDir = dataDirCandidate;
				}
			}
		}
		if (dataDir == null) {
			dataDir = PlatformFactory.getPlatform().getApplicationDataDir(applicationId);
		}
	}
	
	/**
	 * Load and instantiate the LoggingConfiguration
	 * @return
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	private LogConfiguration loadLogConfiguration() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String classname = this.properties.getProperty("feature.logging.impl");
		if (classname == null) {
			classname = DEFAULT_LOGGING_IMPL;
		}
		Class<?> logImplClass = Class.forName(classname);		
		Object logImpl = logImplClass.newInstance();					
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
