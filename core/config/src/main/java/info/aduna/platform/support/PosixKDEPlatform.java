/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
 
package info.aduna.platform.support;

/**
 * Platform implementation for KDE environments under UNIX.
 */
public class PosixKDEPlatform extends PosixPlatform {

	public String getName() {
		return "POSIX-compatible with KDE";
	}
}
