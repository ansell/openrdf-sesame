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
package org.openrdf.repository.sail;

import java.io.File;

import org.openrdf.OpenRDFException;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.base.AbstractRepository;
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
public class ProxyRepository extends AbstractRepository implements RepositoryResolverClient {

	private File dataDir;

	private Repository proxiedRepository;

	private String proxiedID;

	/** independent life cycle */
	private RepositoryResolver resolver;

	public ProxyRepository() {
		super();
	}

	/**
	 * Creates a repository instance that proxies to a repository of the give ID.
	 * 
	 * @param proxiedIdentity
	 *        id of the proxied repository
	 */
	public ProxyRepository(String proxiedIdentity) {
		super();
		this.setProxiedIdentity(proxiedIdentity);
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
		if (resolver == null) {
			throw new RepositoryException("Expected RepositoryResolver to be set.");
		}
		getProxiedRepository().initialize();
	}

	@Override
	protected void shutDownInternal()
		throws RepositoryException
	{
		getProxiedRepository().shutDown();
	}
}
