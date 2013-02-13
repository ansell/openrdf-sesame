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
package org.openrdf.repository.manager;

import java.io.File;

import org.openrdf.OpenRDFException;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.base.RepositoryBase;

/**
 * <p>
 * {@link org.openrdf.repository.Repository} implementation that takes a
 * {@link LocalRepositoryManager} instance and the id of a managed repository,
 * and delegate all calls through to the given repository.
 * </p>
 * <p>
 * The purpose is to allow {@link org.openrdf.sail.federation.Federation} to
 * federate local repositories without having to go through an HTTP layer.
 * </p>
 * <p>
 * The implementation is independent of
 * {@link org.openrdf.repository.DelegatingRepository} so that it is freed from
 * having to provide implementation details in its configuration data, just as
 * {@link org.openrdf.repository.http.HTTPRepository} and
 * {@link org.openrdf.repository.sparql.SPARQLRepository}. Instead, it only has
 * to provide an unambiguous local identifier to the proxy.
 * </p>
 * 
 * @author Dale Visser
 */
public class ProxyRepository extends RepositoryBase {

	private File dataDir;

	private Repository proxiedRepository;

	private String proxiedID;

	private LocalRepositoryManager manager;

	public ProxyRepository() {
		super();
	}

	/**
	 * Creates a repository instance that proxies to the given repository.
	 * 
	 * @param manager
	 *        manager that the proxied repository is associated with
	 * @param proxiedIdentity
	 *        id of the proxied repository
	 */
	public ProxyRepository(LocalRepositoryManager manager, String proxiedIdentity)
	{
		super();
		this.setManager(manager);
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

	protected final void setManager(LocalRepositoryManager manager) {
		if (manager != this.manager) {
			this.manager = manager;
			this.proxiedRepository = null;
		}
	}

	private Repository getProxiedRepository() {
		if (null == proxiedRepository) {
			assert null != manager : "Expected manager to be set.";
			assert null != proxiedID : "Expected proxiedID to be set.";
			try {
				proxiedRepository = manager.getRepository(proxiedID);
			} catch (OpenRDFException ore) {
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
