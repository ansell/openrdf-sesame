/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.openrdf.model.Resource;

/**
 * General utility methods for OpenRDF/Sesame modules.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class OpenRDFUtil {

	private static final Resource[] DEFAULT_CONTEXTS = new Resource[] { null };

	/**
	 * Verifies that the supplied contexts parameter is not <tt>null</tt>,
	 * returning the default context if it is.
	 * 
	 * @param contexts
	 *        The parameter to check.
	 * @returns a none-null array
	 */
	public static Resource[] notNull(Resource... contexts) {
		if (contexts == null) {
			return DEFAULT_CONTEXTS;
		}
		return contexts;
	}

	public static String findVersion(Class<?> app, String groupId, String artifactId) {
		String version = null;

		String properties = "META-INF/maven/" + groupId + "/" + artifactId + "/pom.properties";
		InputStream in = app.getClassLoader().getResourceAsStream(properties);
		if (in != null) {
			try {
				try {
					Properties pom = new Properties();
					pom.load(in);
					version = (String)pom.get("version");
				}
				finally {
					in.close();
				}
			}
			catch (IOException e) {
				System.err.println("ERROR: Unable to read version info " + e.getMessage());
			}
		}

		if (version == null) {
			version = "devel";
		}

		return version;
	}
}
