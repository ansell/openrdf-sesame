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
