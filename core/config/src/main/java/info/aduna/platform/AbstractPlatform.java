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

package info.aduna.platform;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility base class for Platform implementations.
 */
public abstract class AbstractPlatform implements Platform {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected String whitespaceReplacement = "-";

	protected String separatorReplacement = "-";

	protected String colonReplacement = "";

	public File getUserHome() {
		File result = null;

		String userHome = System.getProperty("user.home");
		result = new File(userHome);

		return result;
	}

	public final File getApplicationDataDir() {
		File result;
		String sysProp;

		if ((sysProp = System.getProperty(APPDATA_BASEDIR_PROPERTY)) != null) {
			result = new File(sysProp);
		}
		else if ((sysProp = System.getProperty(OLD_DATADIR_PROPERTY)) != null) {
			logger.info(
					"Old Aduna datadir property \"{}\" detected. This property has been replaced with \"{}\". "
							+ "Support for the old property may be removed in a future version of this application.",
					OLD_DATADIR_PROPERTY, APPDATA_BASEDIR_PROPERTY);

			result = new File(sysProp);
		}
		else {
			result = getOSApplicationDataDir();
		}

		return result;
	}

	public final File getApplicationDataDir(String applicationName) {
		return new File(getApplicationDataDir(), getRelativeApplicationDataDir(applicationName));
	}

	public final File getOSApplicationDataDir(String applicationName) {
		return new File(getOSApplicationDataDir(), getRelativeApplicationDataDir(applicationName));
	}

	public String getRelativeApplicationDataDir(String applicationName) {
		return getRelativeApplicationDataDir(applicationName, dataDirPreserveCase(),
				dataDirReplaceWhitespace(), dataDirReplaceColon());
	}

	public String getRelativeApplicationDataDir(String applicationName, boolean caseSensitive,
			boolean replaceWhitespace, boolean replaceColon)
	{
		String result = applicationName.replace(File.separator, separatorReplacement);

		if (!caseSensitive) {
			result = result.toLowerCase();
		}
		if (replaceWhitespace) {
			result = result.replaceAll("\\s", whitespaceReplacement);
		}
		if (replaceColon) {
			result = result.replace(":", colonReplacement);
		}

		return result;
	}
}
