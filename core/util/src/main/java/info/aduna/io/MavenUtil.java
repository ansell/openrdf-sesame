/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.io;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.LoggerFactory;


/**
 * Maven-related utility methods.
 * 
 * @author Arjohn Kampman
 */
public class MavenUtil {

	/**
	 * Loads the Maven <tt>pom.properties</tt> for the specified artifact.
	 * 
	 * @param groupId
	 *        The artifact's group ID.
	 * @param artifactId
	 *        The artifact's ID.
	 * @return The parsed pom properties, or <tt>null</tt> if the resource could
	 *         not be found.
	 */
	public static Properties loadPomProperties(String groupId, String artifactId)
		throws IOException
	{
		String properties = "META-INF/maven/" + groupId + "/" + artifactId + "/pom.properties";
		return ResourceUtil.getProperties(properties);
	}

	/**
	 * Loads the version number from the <tt>pom.properties</tt> file for the
	 * specified artifact.
	 * 
	 * @param groupId
	 *        The artifact's group ID.
	 * @param artifactId
	 *        The artifact's ID.
	 * @param defaultVersion
	 *        The version number to return in case no version number was found.
	 */
	public static String loadVersion(String groupId, String artifactId, String defaultVersion) {
		String version = null;

		try {
			Properties pom = loadPomProperties(groupId, artifactId);
			if (pom != null) {
				version = pom.getProperty("version");
			}
		}
		catch (IOException e) {
			LoggerFactory.getLogger(MavenUtil.class).warn("Unable to read version info", e);
		}
	
		if (version == null) {
			version = defaultVersion;
		}

		return version;
	}
}
