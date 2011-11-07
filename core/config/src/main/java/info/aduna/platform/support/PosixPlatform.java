/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
 
package info.aduna.platform.support;

import info.aduna.platform.PlatformBase;

import java.io.File;

/**
 * Platform implementation for *nix platforms.
 */
public class PosixPlatform extends PlatformBase {

	public String getName() {
		return "POSIX-compatible";
	}

	public File getOSApplicationDataDir() {
		return new File(System.getProperty("user.home"), ".aduna");
	}

	public boolean dataDirPreserveCase() {
		return false;
	}

	public boolean dataDirReplaceWhitespace() {
		return true;
	}

	public boolean dataDirReplaceColon() {
		return false;
	}
}
