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

import static org.junit.Assert.*;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.Test;

import info.aduna.io.Java7FileUtil;

public class DefaultIndexTest {

	@Test
	public void testDefaultIndex()
		throws Exception
	{
		Path dir = Java7FileUtil.createTempDir("nativerdf");
		TripleStore store = new TripleStore(dir, null);
		store.close();
		// check that the triple store used the default index
		assertEquals("spoc,posc", findIndex(dir));
		Java7FileUtil.deleteDir(dir);
	}

	@Test
	public void testExistingIndex()
		throws Exception
	{
		Path dir = Java7FileUtil.createTempDir("nativerdf");
		// set a non-default index
		TripleStore store = new TripleStore(dir, "spoc,opsc");
		store.close();
		String before = findIndex(dir);
		// check that the index is preserved with a null value
		store = new TripleStore(dir, null);
		store.close();
		assertEquals(before, findIndex(dir));
		Java7FileUtil.deleteDir(dir);
	}

	private String findIndex(Path dir)
		throws Exception
	{
		Properties properties = new Properties();

		try (InputStream in = Files.newInputStream(dir.resolve("triples.prop"));) {
			properties.clear();
			properties.load(in);
		}

		return (String)properties.get("triple-indexes");
	}

}
