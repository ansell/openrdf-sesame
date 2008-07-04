/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import info.aduna.io.FileUtil;

import junit.framework.TestCase;

public class DefaultIndexTest extends TestCase {
	public void testDefaultIndex() throws Exception {
		File dir = FileUtil.createTempDir("nativerdf");
		// set a non-default index
		TripleStore store = new TripleStore(dir, "spoc,opsc");
		store.close();
		String before = findIndex(dir);
		// check that the index is preserved with a null value
		store = new TripleStore(dir, null);
		store.close();
		assertEquals(before, findIndex(dir));
		FileUtil.deleteDir(dir);
	}

	private String findIndex(File dir) throws Exception {
		Properties properties = new Properties();
		InputStream in = new FileInputStream(new File(dir, "triples.prop"));
		try {
			properties.clear();
			properties.load(in);
		}
		finally {
			in.close();
		}
		return (String) properties.get("triple-indexes");
	}

}
