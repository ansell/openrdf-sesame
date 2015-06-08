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
package org.openrdf.sail.nativerdf;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Test;

import info.aduna.io.FileUtil;

public class DefaultIndexTest {

	@Test
	public void testDefaultIndex() throws Exception {
		File dir = FileUtil.createTempDir("nativerdf");
		TripleStore store = new TripleStore(dir, null);
		store.close();
		// check that the triple store used the default index
		assertEquals("spoc,posc", findIndex(dir));
		FileUtil.deleteDir(dir);
	}

	@Test
	public void testExistingIndex() throws Exception {
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
		} finally {
			in.close();
		}
		return (String) properties.get("triple-indexes");
	}

}
