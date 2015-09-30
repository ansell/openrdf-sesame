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
package org.openrdf.console;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.openrdf.OpenRDFException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.sail.config.ProxyRepositoryConfig;

/**
 * @author Dale Visser
 */
public class DropTest extends AbstractCommandTest {

	private static final String MEMORY_MEMBER_ID1 = "alien";

	private static final String PROXY_ID = "proxyID";

	private Drop drop;

	@Rule
	public final TemporaryFolder LOCATION = new TemporaryFolder();

	@Before
	public void prepareManager()
		throws UnsupportedEncodingException, IOException, OpenRDFException
	{
		manager = new LocalRepositoryManager(LOCATION.getRoot());
		manager.initialize();
		addRepositories(MEMORY_MEMBER_ID1);
		manager.addRepositoryConfig(new RepositoryConfig(PROXY_ID, new ProxyRepositoryConfig(MEMORY_MEMBER_ID1)));
		ConsoleState state = mock(ConsoleState.class);
		when(state.getManager()).thenReturn(manager);
		drop = new Drop(streams, state, new Close(streams, state), new LockRemover(streams));
	}

	private void setUserDropConfirm(boolean confirm) throws IOException {
		when(streams.askProceed(startsWith("WARNING: you are about to drop repository '"), anyBoolean())).thenReturn(
				confirm);
	}

	@After
	public void tearDown()
		throws OpenRDFException
	{
		manager.shutDown();
	}

	@Test
	public final void testSafeDrop()
		throws RepositoryException, IOException
	{
		setUserDropConfirm(true);
		assertThat(manager.isSafeToRemove(PROXY_ID), is(equalTo(true)));
		drop.execute("drop", PROXY_ID);
		verify(streams).writeln("Dropped repository '" + PROXY_ID + "'");
		assertThat(manager.isSafeToRemove(MEMORY_MEMBER_ID1), is(equalTo(true)));
		drop.execute("drop", MEMORY_MEMBER_ID1);
		verify(streams).writeln("Dropped repository '" + MEMORY_MEMBER_ID1 + "'");
	}

	@Test
	public final void testUnsafeDropCancel()
		throws RepositoryException, IOException
	{
		setUserDropConfirm(true);
		assertThat(manager.isSafeToRemove(MEMORY_MEMBER_ID1), is(equalTo(false)));
		when(streams.askProceed(startsWith("WARNING: dropping this repository may break"), anyBoolean())).thenReturn(
				false);
		drop.execute("drop", MEMORY_MEMBER_ID1);
		verify(streams).writeln("Drop aborted");
	}

	@Test
	public final void testUserAbortedUnsafeDropBeforeWarning()
		throws IOException
	{
		setUserDropConfirm(false);
		drop.execute("drop", MEMORY_MEMBER_ID1);
		verify(streams, never()).askProceed(startsWith("WARNING: dropping this repository may break"),
				anyBoolean());
		verify(streams).writeln("Drop aborted");
	}
}
