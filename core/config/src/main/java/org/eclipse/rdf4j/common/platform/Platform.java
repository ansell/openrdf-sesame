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
 
package org.eclipse.rdf4j.common.platform;

import java.io.File;

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
	public File getOSApplicationDataDir();

	/**
	 * Returns the operating system dependend application data dir. This will be
	 * a sub-directory of the directory returned by the no-argument version of
	 * this method.
	 */
	public File getOSApplicationDataDir(String applicationName);

	/**
	 * Returns the directory for the current user.
	 * 
	 * @return the current user home directory
	 */
	public File getUserHome();

	/**
	 * Returns the directory in which Aduna applications can store their
	 * application-dependent data, returns 'getOSApplicationDataDir' unless the
	 * system property "aduna.platform.applicationdata.dir" has been set.
	 * 
	 * @return the Aduna-specific application data directory
	 */
	public File getApplicationDataDir();

	/**
	 * Returns the directory in which a specific application can store all its
	 * application-dependent data. This will be a sub-directory of the directory
	 * returned by the no-argument version of this method. Note: the directory
	 * might not exist yet.
	 * 
	 * @see #getApplicationDataDir()
	 * @param applicationName
	 *            the name of the application for which to determine the
	 *            directory
	 * @return an application-specific data directory
	 */
	public File getApplicationDataDir(String applicationName);
	
	/**
	 * Get the directory relative to getApplicationDataDir() for the specified application.
	 * @param applicationName the name of the application
	 * @return the directory relative to getApplicationDataDir() for the specified application
	 */
	public String getRelativeApplicationDataDir(String applicationName);
	
	public boolean dataDirPreserveCase();
	
	public boolean dataDirReplaceWhitespace();

	public boolean dataDirReplaceColon();
}
