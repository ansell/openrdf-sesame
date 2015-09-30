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
package org.eclipse.rdf4j.sail.nativerdf;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.rdf4j.common.io.FileUtil;
import org.eclipse.rdf4j.sail.nativerdf.TripleStore;
import org.junit.Test;

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
