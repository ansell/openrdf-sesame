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
package org.eclipse.rdf4j.repository.sail;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.sail.ProxyRepository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.repository.sail.config.RepositoryResolver;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TestProxyRepository {

	private ProxyRepository repository;

	private final SailRepository proxied = new SailRepository(new MemoryStore());

	@Rule
	public final TemporaryFolder dataDir = new TemporaryFolder();

	@Before
	public final void setUp()
		throws RepositoryConfigException, RepositoryException
	{
		RepositoryResolver resolver = mock(RepositoryResolver.class);
		when(resolver.getRepository("test")).thenReturn(proxied);
		repository = new ProxyRepository(resolver, "test");
		repository.setDataDir(dataDir.getRoot());
	}

	@After
	public final void tearDown()
		throws RepositoryException
	{
		repository.shutDown();
	}

	@Test(expected = IllegalStateException.class)
	public final void testDisallowAccessBeforeInitialize()
		throws RepositoryException
	{
		repository.getConnection();
	}

	@Test
	public final void testProperInitialization()
		throws RepositoryException
	{
		assertThat(repository.getDataDir(), is(dataDir.getRoot()));
		assertThat(repository.getProxiedIdentity(), is("test"));
		assertThat(repository.isInitialized(), is(false));
		assertThat(repository.isWritable(), is(proxied.isWritable()));
		repository.initialize();
		RepositoryConnection connection = repository.getConnection();
		try {
			assertThat(connection, instanceOf(SailRepositoryConnection.class));
		}
		finally {
			connection.close();
		}
	}

	@Test(expected = IllegalStateException.class)
	public final void testNoAccessAfterShutdown()
		throws RepositoryException
	{
		repository.initialize();
		repository.shutDown();
		repository.getConnection();
	}

	@Test
	public final void addDataToProxiedAndCompareToProxy()
		throws RepositoryException, RDFParseException, IOException
	{
		proxied.initialize();
		RepositoryConnection connection = proxied.getConnection();
		long count;
		try {
			connection.add(Thread.currentThread().getContextClassLoader().getResourceAsStream("proxy.ttl"),
					"http://www.test.org/proxy#", RDFFormat.TURTLE);
			count = connection.size();
			assertThat(count, not(0L));
		}
		finally {
			connection.close();
		}
		connection = repository.getConnection();
		try {
			assertThat(connection.size(), is(count));
		}
		finally {
			connection.close();
		}
	}
}