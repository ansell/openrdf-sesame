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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.platform.support.MacOSXPlatform;
import info.aduna.platform.support.PosixGnomePlatform;
import info.aduna.platform.support.PosixKDEPlatform;
import info.aduna.platform.support.PosixPlatform;
import info.aduna.platform.support.WindowsPlatform;

/**
 * PlatformFactory creates a Platform instance corresponding with the current
 * platform.
 */
public class PlatformFactory {

	private static PlatformFactory sharedInstance;

	/**
	 * Returns the Platform instance corresponding with the current platform.
	 */
	public static PlatformFactory getInstance() {
		if (sharedInstance == null) {
			sharedInstance = new PlatformFactory();
		}
		return sharedInstance;
	}

	/**
	 * Returns the Platform instance corresponding with the current platform.
	 */
	public static Platform getPlatform() {
		return getInstance().platform;
	}

	/*-----------*
	 * Constants *
	 *-----------*/

	public final Platform platform;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/*--------------*
	 * Constructors *
	 *--------------*/

	private PlatformFactory() {
		platform = createPlatform();
	}

	/**
	 * Tries to determine the platform we're running on based on Java system
	 * properties and/or environment variables. See
	 * http://lopica.sourceforge.net/os.html for an overview.
	 */
	private Platform createPlatform() {
		Platform platform;

		try {
			String osName = System.getProperty("os.name");

			if (osName != null) {
				osName = osName.toLowerCase();

				logger.debug("os.name = {}", osName);

				if (osName.contains("windows")) {
					logger.debug("Detected Windows platform");
					platform = new WindowsPlatform();
				}
				else if (osName.contains("solaris") || osName.contains("sunos") || osName.contains("linux")
						|| osName.contains("hp-ux"))
				{
					// Try to detect specific window managers
					if (isGnome()) {
						logger.debug("Detected Gnome window manager on Posix platform");
						platform = new PosixGnomePlatform();
					}
					else if (isKDE()) {
						logger.debug("Detected KDE window manager on Posix platform");
						platform = new PosixKDEPlatform();
					}
					else {
						logger.debug("Detected Posix platform");
						platform = new PosixPlatform();
					}
				}
				else if (osName.contains("mac os x") || osName.contains("macos") || osName.contains("darwin") ||
						System.getProperty("mrj.version") != null) {
					logger.debug("Detected Mac OS X platform");
					platform = new MacOSXPlatform();
				}
				else {
					logger.warn("Unrecognized operating system: '{}', falling back to default platform", osName);
					platform = new DefaultPlatform();
				}
			}
			else {
				logger.warn("System property 'os.name' is null, falling back to default platform");
				platform = new DefaultPlatform();
			}
		}
		catch (SecurityException e) {
			logger.warn("Not allowed to read system property 'os.name', falling back to default platform", e);
			platform = new DefaultPlatform();
		}

		return platform;
	}

	/**
	 * Detect gnome environments.
	 */
	private boolean isGnome() {
		// check gdm session
		String gdmSession = getSystemEnv("GDMSESSION");
		if (gdmSession != null && gdmSession.toLowerCase().contains("gnome")) {
			return true;
		}

		// check desktop session
		String desktopSession = getSystemEnv("DESKTOP_SESSION");
		if (desktopSession != null && desktopSession.toLowerCase().contains("gnome")) {
			return true;
		}

		// check gnome desktop id
		String gnomeDesktopSessionId = getSystemEnv("GNOME_DESKTOP_SESSION_ID");
		if (gnomeDesktopSessionId != null && gnomeDesktopSessionId.trim().length() > 0) {
			return true;
		}

		return false;
	}

	/**
	 * Detect KDE environments.
	 */
	private boolean isKDE() {
		// check gdm session
		String gdmSession = getSystemEnv("GDMSESSION");
		if (gdmSession != null && gdmSession.toLowerCase().contains("kde")) {
			return true;
		}

		// check desktop session
		String desktopSession = getSystemEnv("DESKTOP_SESSION");
		if (desktopSession != null && desktopSession.toLowerCase().contains("kde")) {
			return true;
		}

		// check window manager
		String windowManager = getSystemEnv("WINDOW_MANAGER");
		if (windowManager != null && windowManager.trim().toLowerCase().endsWith("kde")) {
			return true;
		}

		return false;
	}

	private String getSystemEnv(String propertyName) {
		try {
			return System.getenv(propertyName);
		}
		catch (SecurityException e) {
			logger.warn("Not allowed to read environment variable '" + propertyName + "'", e);
			return null;
		}
	}

	public static void main(String[] args) {
		System.out.println(getPlatform().getApplicationDataDir("My Application: Test").getAbsolutePath());
	}
}
