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
package org.openrdf.repository.sail.config;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.junit.Test;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.sail.ProxyRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;

public class TestProxyRepositoryFactory {

	private final ProxyRepositoryFactory factory = new ProxyRepositoryFactory();

	@Test
	public final void testGetRepositoryType() {
		assertThat(factory.getRepositoryType(), is("openrdf:ProxyRepository"));
	}

	@Test(expected = RepositoryConfigException.class)
	public final void testGetConfig()
		throws RepositoryConfigException
	{
		RepositoryImplConfig factoryConfig = factory.getConfig();
		assertThat(factoryConfig, instanceOf(ProxyRepositoryConfig.class));
		factoryConfig.validate();
	}

	@Test
	public final void testGetRepository()
		throws OpenRDFException, IOException
	{
		Model graph = Rio.parse(this.getClass().getResourceAsStream("/proxy.ttl"),
				RepositoryConfigSchema.NAMESPACE, RDFFormat.TURTLE);
		RepositoryConfig config = RepositoryConfig.create(graph,
				GraphUtil.getUniqueSubject(graph, RDF.TYPE, RepositoryConfigSchema.REPOSITORY));
		config.validate();
		assertThat(config.getID(), is("proxy"));
		assertThat(config.getTitle(), is("Test Proxy for 'memory'"));
		RepositoryImplConfig implConfig = config.getRepositoryImplConfig();
		assertThat(implConfig.getType(), is("openrdf:ProxyRepository"));
		assertThat(implConfig, instanceOf(ProxyRepositoryConfig.class));
		assertThat(((ProxyRepositoryConfig)implConfig).getProxiedRepositoryID(), is("memory"));

		// Factory just needs a resolver instance to proceed with construction.
		// It doesn't actually invoke the resolver until the repository is
		// accessed. Normally LocalRepositoryManager is the caller of
		// getRepository(), and will have called this setter itself.
		ProxyRepository repository = (ProxyRepository)factory.getRepository(implConfig);
		repository.setRepositoryResolver(mock(RepositoryResolver.class));
		assertThat(repository, instanceOf(ProxyRepository.class));
	}
}
