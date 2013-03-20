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

import java.io.File;

import org.openrdf.OpenRDFException;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.base.RepositoryBase;
import org.openrdf.repository.sail.config.RepositoryResolver;
import org.openrdf.repository.sail.config.RepositoryResolverClient;

/**
 * <p>
 * {@link org.openrdf.repository.Repository} implementation that takes a
 * {@link org.openrdf.repository.sail.config.RepositoryResolver} instance and
 * the id of a managed repository, and delegate all calls through to the given
 * repository.
 * </p>
 * <p>
 * The purpose is to allow {@link org.openrdf.sail.Sail}s to refer to other
 * local repositories using a unique identifier without having to go through an
 * HTTP layer.
 * </p>
 * <p>
 * The implementation is independent of
 * {@link org.openrdf.repository.DelegatingRepository} so that it is freed from
 * having to provide implementation details in its configuration data. Instead,
 * it only has to provide an unambiguous local identifier to the proxy.
 * </p>
 * 
 * @author Dale Visser
 */
public class ProxyRepository extends RepositoryBase implements RepositoryResolverClient {

	private File dataDir;

	private Repository proxiedRepository;

	private String proxiedID;

	private RepositoryResolver resolver;

	public ProxyRepository() {
		super();
	}

	/**
	 * Creates a repository instance that proxies to the given repository.
	 * 
	 * @param resolver
	 *        manager that the proxied repository is associated with
	 * @param proxiedIdentity
	 *        id of the proxied repository
	 */
	public ProxyRepository(RepositoryResolver resolver, String proxiedIdentity) {
		super();
		this.setRepositoryResolver(resolver);
		this.setProxiedIdentity(proxiedIdentity);
	}

	public final void setProxiedIdentity(String value) {
		if (!value.equals(this.proxiedID)) {
			this.proxiedID = value;
			this.proxiedRepository = null;
		}
	}

	public String getProxiedIdentity() {
		return this.proxiedID;
	}

	@Override
	public final void setRepositoryResolver(RepositoryResolver resolver) {
		if (resolver != this.resolver) {
			this.resolver = resolver;
			this.proxiedRepository = null;
		}
	}

	private Repository getProxiedRepository() {
		if (null == proxiedRepository) {
			assert null != resolver : "Expected resolver to be set.";
			assert null != proxiedID : "Expected proxiedID to be set.";
			try {
				proxiedRepository = resolver.getRepository(proxiedID);
			}
			catch (OpenRDFException ore) {
				throw new IllegalStateException(ore);
			}
		}
		return proxiedRepository;
	}

	@Override
	public void setDataDir(File dataDir) {
		this.dataDir = dataDir;
	}

	@Override
	public File getDataDir() {
		return this.dataDir;
	}

	@Override
	public boolean isWritable()
		throws RepositoryException
	{
		return getProxiedRepository().isWritable();
	}

	@Override
	public RepositoryConnection getConnection()
		throws RepositoryException
	{
		return getProxiedRepository().getConnection();
	}

	@Override
	public ValueFactory getValueFactory() {
		return getProxiedRepository().getValueFactory();
	}

	@Override
	protected void initializeInternal()
		throws RepositoryException
	{
		getProxiedRepository().initialize();
	}

	@Override
	protected void shutDownInternal()
		throws RepositoryException
	{
		getProxiedRepository().shutDown();
	}
}
