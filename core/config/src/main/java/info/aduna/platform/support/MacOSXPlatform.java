/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
 
package info.aduna.platform.support;

import java.io.File;

/**
 * Platform implementation for Mac OS X platforms.
 */
public class MacOSXPlatform extends PosixPlatform {

	public static final String APPLICATION_DATA = "Library/Application Support/Aduna";
	
	public String getName() {
		return "Mac OS X";
	}
	
	@Override
	public File getOSApplicationDataDir() {
		return new File(System.getProperty("user.home"), APPLICATION_DATA);
	}

	public boolean dataDirPreserveCase() {
		return true;
	}

	public boolean dataDirReplaceWhitespace() {
		return false;
	}	
}
