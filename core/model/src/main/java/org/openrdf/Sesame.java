/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf;

import info.aduna.io.MavenUtil;

/**
 * @author Arjohn Kampman
 */
public class Sesame {

	private static final String VERSION = MavenUtil.loadVersion("org.openrdf.sesame", "sesame-console", "dev");
	
	public static String getVersion() {
		return VERSION;
	}
}
