/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
 
package info.aduna.platform;

import java.io.File;

public class DefaultPlatform extends PlatformBase {

	public String getName() {
		return "Default";
	}

	public File getOSApplicationDataDir() {
		return new File("Aduna");
	}

	public boolean dataDirPreserveCase() {
		return false;
	}

	public boolean dataDirReplaceWhitespace() {
		return false;
	}

	public boolean dataDirReplaceColon() {
		return false;
	}
}
