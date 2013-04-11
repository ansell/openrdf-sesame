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
package info.aduna.app.logging.logback;

import static org.junit.Assert.*;

import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test for {@link LogbackConfiguration}.
 * 
 * @author Peter Ansell
 */
public class LogbackConfigurationTest {

	@Rule
	public TemporaryFolder tempDir = new TemporaryFolder();

	private LogbackConfiguration config;

	private Path baseDir;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		this.baseDir = tempDir.newFolder().toPath();
		this.config = new LogbackConfiguration();
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
	 * {@link info.aduna.app.logging.logback.LogbackConfiguration#LogbackConfiguration()}
	 * .
	 */
	@Ignore
	@Test
	public final void testLogbackConfiguration()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link info.aduna.app.logging.logback.LogbackConfiguration#init()}.
	 */
	@Test
	public final void testInit()
		throws Exception
	{
		this.config.setBaseDir(baseDir);
		this.config.init();
	}

	/**
	 * Test method for
	 * {@link info.aduna.app.logging.logback.LogbackConfiguration#load()}.
	 */
	@Ignore
	@Test
	public final void testLoad()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link info.aduna.app.logging.logback.LogbackConfiguration#save()}.
	 */
	@Ignore
	@Test
	public final void testSave()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link info.aduna.app.logging.logback.LogbackConfiguration#destroy()}.
	 */
	@Ignore
	@Test
	public final void testDestroy()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link info.aduna.app.logging.logback.LogbackConfiguration#getLogReader(java.lang.String)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetLogReader()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link info.aduna.app.logging.logback.LogbackConfiguration#getDefaultLogReader()}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetDefaultLogReader()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

}
