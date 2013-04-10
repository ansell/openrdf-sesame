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

package info.aduna.platform;

import java.io.File;
import java.nio.file.Path;

/**
 * The Platform interface defines methods that are expected to differ slightly
 * between operating systems, e.g. methods for opening local files, storing
 * application data, etc.
 */
public interface Platform {

	public static final String APPDATA_BASEDIR_PROPERTY = "info.aduna.platform.appdata.basedir";

	@Deprecated
	public static final String OLD_DATADIR_PROPERTY = "aduna.platform.applicationdata.dir";

	/**
	 * Get a descriptive name for this platform.
	 */
	public String getName();

	/**
	 * Returns the operating system dependend application data dir.
	 */
	public Path getOSApplicationDataDir();

	/**
	 * Returns the operating system dependend application data dir. This will be
	 * a sub-directory of the directory returned by the no-argument version of
	 * this method.
	 */
	public Path getOSApplicationDataDir(String applicationName);

	/**
	 * Returns the directory for the current user.
	 * 
	 * @return the current user home directory
	 */
	public Path getUserHome();

	/**
	 * Returns the directory in which Aduna applications can store their
	 * application-dependent data, returns 'getOSApplicationDataDir' unless the
	 * system property "aduna.platform.applicationdata.dir" has been set.
	 * 
	 * @return the Aduna-specific application data directory
	 */
	public Path getApplicationDataDir();

	/**
	 * Returns the directory in which a specific application can store all its
	 * application-dependent data. This will be a sub-directory of the directory
	 * returned by the no-argument version of this method. Note: the directory
	 * might not exist yet.
	 * 
	 * @see #getApplicationDataDir()
	 * @param applicationName
	 *        the name of the application for which to determine the directory
	 * @return an application-specific data directory
	 */
	public Path getApplicationDataDir(String applicationName);

	/**
	 * Get the directory relative to getApplicationDataDir() for the specified
	 * application.
	 * 
	 * @param applicationName
	 *        the name of the application
	 * @return the directory relative to getApplicationDataDir() for the
	 *         specified application
	 */
	public String getRelativeApplicationDataDir(String applicationName);

	public boolean dataDirPreserveCase();

	public boolean dataDirReplaceWhitespace();

	public boolean dataDirReplaceColon();
}
