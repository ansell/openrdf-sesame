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
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility base class for Platform implementations.
 */
public abstract class PlatformBase implements Platform {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected String whitespaceReplacement = "-";

	protected String separatorReplacement = "-";

	protected String colonReplacement = "";

	@Override
	public Path getUserHome() {
		Path result = null;

		String userHome = System.getProperty("user.home");
		result = Paths.get(userHome);

		return result;
	}

	@Override
	public final Path getApplicationDataDir() {
		Path result;
		String sysProp;

		if ((sysProp = System.getProperty(APPDATA_BASEDIR_PROPERTY)) != null) {
			result = Paths.get(sysProp);
		}
		else if ((sysProp = System.getProperty(OLD_DATADIR_PROPERTY)) != null) {
			logger.info(
					"Old Aduna datadir property \"{}\" detected. This property has been replaced with \"{}\". "
							+ "Support for the old property may be removed in a future version of this application.",
					OLD_DATADIR_PROPERTY, APPDATA_BASEDIR_PROPERTY);

			result = Paths.get(sysProp);
		}
		else {
			result = getOSApplicationDataDir();
		}

		return result;
	}

	@Override
	public final Path getApplicationDataDir(String applicationName) {
		return getApplicationDataDir().resolve(getRelativeApplicationDataDir(applicationName));
	}

	@Override
	public final Path getOSApplicationDataDir(String applicationName) {
		return getOSApplicationDataDir().resolve(getRelativeApplicationDataDir(applicationName));
	}

	@Override
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
