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
package org.openrdf.sail.helpers;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import info.aduna.concurrent.locks.Lock;

/**
 * @author Peter Ansell
 */
public class DirectoryLockManagerTest {

	@Rule
	public TemporaryFolder tempDir = new TemporaryFolder();

	private Path testDir;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		this.testDir = tempDir.newFolder("directory-lock-manager-test").toPath();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown()
		throws Exception
	{
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.helpers.DirectoryLockManager#DirectoryLockManager(java.nio.file.Path)}
	 * .
	 */
	@Ignore
	@Test
	public final void testDirectoryLockManager()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.helpers.DirectoryLockManager#getLocation()}.
	 */
	@Ignore
	@Test
	public final void testGetLocation()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.helpers.DirectoryLockManager#isLocked()}.
	 */
	@Ignore
	@Test
	public final void testIsLocked()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.helpers.DirectoryLockManager#tryLock()}.
	 */
	@Ignore
	@Test
	public final void testTryLock()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.helpers.DirectoryLockManager#lockOrFail()}.
	 */
	@Test
	public final void testLockOrFail()
		throws Exception
	{
		assertNotNull(testDir);

		assertTrue(Files.exists(testDir));
		assertTrue(Files.isDirectory(testDir));
		assertTrue(Files.isReadable(testDir));

		// try to lock the directory or fail
		Lock dirLock = new DirectoryLockManager(testDir).lockOrFail();

	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.helpers.DirectoryLockManager#revokeLock()}.
	 */
	@Ignore
	@Test
	public final void testRevokeLock()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

}
