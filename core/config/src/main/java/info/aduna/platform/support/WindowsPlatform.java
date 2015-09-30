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

package info.aduna.platform.support;

import java.io.File;

import info.aduna.platform.AbstractPlatform;
import info.aduna.platform.ProcessLauncher;

/**
 * Platform implementation for all Windows' platforms.
 */
public class WindowsPlatform extends AbstractPlatform {

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

	public File getUserHome() {
		File result = super.getUserHome();

		String homeDrive = System.getenv("HOMEDRIVE");
		String homePath = System.getenv("HOMEPATH");
		if (homeDrive != null && homePath != null) {
			File homeDir = new File(homeDrive + homePath);
			if (homeDir.isDirectory() && homeDir.canWrite()) {
				result = homeDir;
			}
		}
		else {
			String userProfile = System.getenv("USERPROFILE");
			if (userProfile != null) {
				File userProfileDir = new File(userProfile);
				if (userProfileDir.isDirectory() && userProfileDir.canWrite()) {
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
	public File getOSApplicationDataDir() {
		File result = new File(getUserHome(), APPLICATION_DATA);

		// check for the APPDATA environment variable
		String appData = System.getenv("APPDATA");
		if (appData != null) {
			File appDataDir = new File(appData);
			if (appDataDir.isDirectory() && appDataDir.canWrite()) {
				result = appDataDir;
			}
		}

		return new File(result, ADUNA_APPLICATION_DATA);
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
