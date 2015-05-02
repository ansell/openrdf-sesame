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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import info.aduna.io.FileUtil;
import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailException;


/**
 *
 * @author James Leigh
 */
public class TestNativeStoreUpgrade {
	private static final String ZIP_2_7_15 = "/nativerdf-2.7.15.zip";
	private static final String ZIP_2_7_15_INCONSISTENT = "/nativerdf-inconsistent-2.7.15.zip";

	private File dataDir;

	@Before
	public void setUp()
		throws Exception
	{
		dataDir = FileUtil.createTempDir("nativestore");
	}

	@After
	public void tearDown()
		throws Exception
	{
		FileUtil.deleteDir(dataDir);
		dataDir = null;
	}

	@Test
	public void testDevel() throws IOException, SailException {
		NativeStore store = new NativeStore(dataDir);
		try {
			store.initialize();
			NotifyingSailConnection con = store.getConnection();
			try {
				ValueFactory vf = store.getValueFactory();
				con.begin();
				con.addStatement(RDF.VALUE, RDFS.LABEL, vf.createLiteral("value"));
				con.commit();
			} finally {
				con.close();
			}
		} finally {
			store.shutDown();
		}
		new File(dataDir, "nativerdf.ver").delete();
		assertValue(dataDir);
		assertTrue(new File(dataDir, "nativerdf.ver").exists());
	}

	@Test
	public void test2715() throws IOException, SailException {
		extractZipResource(ZIP_2_7_15, dataDir);
		assertFalse(new File(dataDir, "nativerdf.ver").exists());
		assertValue(dataDir);
		assertTrue(new File(dataDir, "nativerdf.ver").exists());
	}

	@Test
	public void test2715Inconsistent() throws IOException, SailException {
		extractZipResource(ZIP_2_7_15_INCONSISTENT, dataDir);
		assertFalse(new File(dataDir, "nativerdf.ver").exists());
		try {
			NativeStore store = new NativeStore(dataDir);
			try {
				store.initialize();
				fail();
			} finally {
				store.shutDown();
			}
		} catch (SailException e) {
			// inconsistent
		}
	}

	public void assertValue(File dataDir)
		throws SailException
	{
		NativeStore store = new NativeStore(dataDir);
		try {
			store.initialize();
			NotifyingSailConnection con = store.getConnection();
			try {
				ValueFactory vf = store.getValueFactory();
				CloseableIteration<? extends Statement, SailException> iter;
				iter = con.getStatements(RDF.VALUE, RDFS.LABEL, vf.createLiteral("value"), false);
				try {
					assertTrue(iter.hasNext());
				} finally {
					iter.close();
				}
			} finally {
				con.close();
			}
		} finally {
			store.shutDown();
		}
	}

	public void extractZipResource(String resource, File dir)
		throws IOException
	{
		InputStream in = TestNativeStoreUpgrade.class.getResourceAsStream(resource);
		try {
			ZipInputStream zip = new ZipInputStream(in);
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				File file = new File(dir, entry.getName());
				file.createNewFile();
				FileChannel ch = FileChannel.open(Paths.get(file.toURI()), StandardOpenOption.WRITE);
				ch.transferFrom(Channels.newChannel(zip), 0, entry.getSize());
				zip.closeEntry();
			}
		} finally {
			in.close();
		}
	}

}