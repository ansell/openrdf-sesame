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
	public void testDevel()
		throws IOException, SailException
	{
		NativeStore store = new NativeStore(dataDir);
		try {
			store.initialize();
			NotifyingSailConnection con = store.getConnection();
			try {
				ValueFactory vf = store.getValueFactory();
				con.begin();
				con.addStatement(RDF.VALUE, RDFS.LABEL, vf.createLiteral("value"));
				con.commit();
			}
			finally {
				con.close();
			}
		}
		finally {
			store.shutDown();
		}
		new File(dataDir, "nativerdf.ver").delete();
		assertValue(dataDir);
		assertTrue(new File(dataDir, "nativerdf.ver").exists());
	}

	@Test
	public void test2715()
		throws IOException, SailException
	{
		extractZipResource(ZIP_2_7_15, dataDir);
		assertFalse(new File(dataDir, "nativerdf.ver").exists());
		assertValue(dataDir);
		assertTrue(new File(dataDir, "nativerdf.ver").exists());
	}

	@Test
	public void test2715Inconsistent()
		throws IOException, SailException
	{
		extractZipResource(ZIP_2_7_15_INCONSISTENT, dataDir);
		assertFalse(new File(dataDir, "nativerdf.ver").exists());
		NativeStore store = new NativeStore(dataDir);
		try {
			store.initialize();
			// we expect init to still succeed, but the store not to be marked as upgraded. See SES-2244.
			assertFalse(new File(dataDir, "nativerdf.ver").exists());
		}
		finally {
			store.shutDown();
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
				}
				finally {
					iter.close();
				}
			}
			finally {
				con.close();
			}
		}
		finally {
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
		}
		finally {
			in.close();
		}
	}

}