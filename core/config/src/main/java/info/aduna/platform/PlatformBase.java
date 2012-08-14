/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */

package info.aduna.platform;

import java.io.File;

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
