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

package info.aduna.platform.support;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import info.aduna.platform.PlatformBase;
import info.aduna.platform.ProcessLauncher;

/**
 * Platform implementation for all Windows' platforms.
 */
public class WindowsPlatform extends PlatformBase {

	/** name of the directory containing application data */
	public static final String APPLICATION_DATA = "Application Data";

	/** name of the app data subdirectory containing all Aduna files * */
	public static final String ADUNA_APPLICATION_DATA = "Aduna";

	/**
	 * indication whether this is a windows9x platform: 0 means not initialized,
	 * -1 means false, 1 means true
	 */
	private int isWin9x = 0;

	/**
	 * Returns the name of this windows platform.
	 */
	@Override
	public String getName() {
		if (isWin9x()) {
			return "Windows 9x";
		}
		else if (isWinNT()) {
			return "Windows NT";
		}
		else if (isWin2000()) {
			return "Windows 2000";
		}
		else if (isWinXP()) {
			return "Windows XP";
		}
		else if (isWin2003()) {
			return "Windows 2003";
		}
		else if (isWinVista()) {
			return "Windows Vista";
		}
		else {
			return "Windows";
		}
	}

	@Override
	public Path getUserHome() {
		Path result = super.getUserHome();

		String homeDrive = System.getenv("HOMEDRIVE");
		String homePath = System.getenv("HOMEPATH");
		if (homeDrive != null && homePath != null) {
			Path homeDir = Paths.get(homeDrive, homePath);
			if (Files.isDirectory(homeDir) && Files.isWritable(homeDir)) {
				result = homeDir;
			}
		}
		else {
			String userProfile = System.getenv("USERPROFILE");
			if (userProfile != null) {
				Path userProfileDir = Paths.get(userProfile);
				if (Files.isDirectory(userProfileDir) && Files.isWritable(userProfileDir)) {
					result = userProfileDir;
				}
			}
		}

		return result;
	}

	/**
	 * Returns an application data directory in the "Application Data" userdir of
	 * Windows.
	 */
	@Override
	public Path getOSApplicationDataDir() {
		Path result = getUserHome().resolve(APPLICATION_DATA);

		// check for the APPDATA environment variable
		String appData = System.getenv("APPDATA");
		if (appData != null) {
			Path appDataDir = Paths.get(appData);
			if (Files.isDirectory(appDataDir) && Files.isWritable(appDataDir)) {
				result = appDataDir;
			}
		}

		return result.resolve(ADUNA_APPLICATION_DATA);
	}

	/**
	 * Returns true when the platform is not a Windows 9x platform.
	 */
	public boolean warnsWhenOpeningExecutable() {
		return !isWin9x() && !isWinNT() && !isWin2000();
	}

	/**
	 * Check whether this is windows 9x, or windows NT and higher.
	 */
	public boolean isWin9x() {
		if (isWin9x == 0) {
			// let's see if this is windows 9x
			try {
				ProcessLauncher launcher = new ProcessLauncher(new String[] { "cmd", "/c", "echo" });
				launcher.launch();
				isWin9x = -1;
			}
			catch (ProcessLauncher.CommandNotExistsException nosuchcommand) {
				isWin9x = 1;
			}
			catch (Exception e) {
				logger.error("Unexpected exception while checking isWin9x", e);
			}
		}
		return isWin9x == 1;
	}

	/**
	 * Check whether this is an Windows NT environment.
	 */
	public boolean isWinNT() {
		return System.getProperty("os.name").toLowerCase().indexOf("nt") >= 0;
	}

	/**
	 * Check whether this is an Windows 2000 environment.
	 */
	public boolean isWin2000() {
		return System.getProperty("os.name").indexOf("2000") >= 0;
	}

	/**
	 * Check whether this is an Windows NT environment.
	 */
	public boolean isWinXP() {
		return System.getProperty("os.name").toLowerCase().indexOf("xp") >= 0;
	}

	/**
	 * Check whether this is an Windows 2003 environment.
	 */
	public boolean isWin2003() {
		return System.getProperty("os.name").indexOf("2003") >= 0;
	}

	/**
	 * Check whether this is an Windows Vista environment.
	 */
	public boolean isWinVista() {
		return System.getProperty("os.name").indexOf("Vista") >= 0;
	}

	/**
	 * Returns appropriate command shell for the current windows shell.
	 */
	public String getCommandShell() {
		if (isWin9x()) {
			return "command.com";
		}
		else {
			return "cmd";
		}
	}

	public boolean dataDirPreserveCase() {
		return true;
	}

	public boolean dataDirReplaceWhitespace() {
		return false;
	}

	public boolean dataDirReplaceColon() {
		return true;
	}
}
