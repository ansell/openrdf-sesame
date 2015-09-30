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
package org.eclipse.rdf4j.common.io;

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
