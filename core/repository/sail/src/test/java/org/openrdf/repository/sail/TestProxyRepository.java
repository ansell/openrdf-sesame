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
package org.openrdf.repository.sail;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.repository.sail.config.RepositoryResolver;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

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
		repository.setDataDir(dataDir.getRoot().toPath());
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
		assertThat(repository.getDataDir(), is(dataDir.getRoot().toPath()));
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