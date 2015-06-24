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

import org.apache.http.client.HttpClient;

import org.openrdf.http.client.HttpClientDependent;
import org.openrdf.http.client.SesameClient;
import org.openrdf.http.client.SesameClientDependent;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.federation.FederatedServiceResolver;
import org.openrdf.query.algebra.evaluation.federation.FederatedServiceResolverClient;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryLockedException;
import org.openrdf.repository.base.RepositoryBase;
import org.openrdf.repository.sail.config.RepositoryResolver;
import org.openrdf.repository.sail.config.RepositoryResolverClient;
import org.openrdf.sail.AdvancedSail;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.SailLockedException;
import org.openrdf.sail.StackableSail;

/**
 * An implementation of the {@link Repository} interface that operates on a
 * (stack of) {@link Sail Sail} object(s). The behaviour of the repository is
 * determined by the Sail stack that it operates on; for example, the repository
 * will only support RDF Schema or OWL semantics if the Sail stack includes an
 * inferencer for this.
 * <p>
 * Creating a repository object of this type is very easy. For example, the
 * following code creates and initializes a main-memory store with RDF Schema
 * semantics:
 * 
 * <pre>
 * Repository repository = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
 * repository.initialize();
 * </pre>
 * 
 * Or, alternatively:
 * 
 * <pre>
 * Sail sailStack = new MemoryStore();
 * sailStack = new ForwardChainingRDFSInferencer(sailStack);
 * 
 * Repository repository = new SailRepository(sailStack);
 * repository.initialize();
 * </pre>
 * 
 * @author Arjohn Kampman
 */
public class SailRepository extends RepositoryBase implements FederatedServiceResolverClient,
		RepositoryResolverClient, HttpClientDependent, SesameClientDependent
{

	/*-----------*
	 * Constants *
	 *-----------*/

	private final Sail sail;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new repository object that operates on the supplied Sail.
	 * 
	 * @param sail
	 *        A Sail object.
	 */
	public SailRepository(Sail sail) {
		this.sail = sail;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public File getDataDir() {
		return sail.getDataDir();
	}

	public void setDataDir(File dataDir) {
		sail.setDataDir(dataDir);
	}

	@Override
	public void setFederatedServiceResolver(FederatedServiceResolver resolver) {
		FederatedServiceResolverClient stack = findSailOf(sail, FederatedServiceResolverClient.class);
		if (stack != null) {
			stack.setFederatedServiceResolver(resolver);
		}
	}

	@Override
	public void setRepositoryResolver(RepositoryResolver resolver) {
		RepositoryResolverClient stack = findSailOf(sail, RepositoryResolverClient.class);
		if (stack != null) {
			stack.setRepositoryResolver(resolver);
		}
	}

	@Override
	public SesameClient getSesameClient() {
		SesameClientDependent stack = findSailOf(sail, SesameClientDependent.class);
		if (stack != null) {
			return stack.getSesameClient();
		}
		else {
			return null;
		}
	}

	@Override
	public void setSesameClient(SesameClient client) {
		SesameClientDependent stack = findSailOf(sail, SesameClientDependent.class);
		if (stack != null) {
			stack.setSesameClient(client);
		}
	}

	@Override
	public HttpClient getHttpClient() {
		HttpClientDependent stack = findSailOf(sail, HttpClientDependent.class);
		if (stack != null) {
			return stack.getHttpClient();
		}
		else {
			return null;
		}
	}

	@Override
	public void setHttpClient(HttpClient client) {
		HttpClientDependent stack = findSailOf(sail, HttpClientDependent.class);
		if (stack != null) {
			stack.setHttpClient(client);
		}
	}

	@Override
	protected void initializeInternal()
		throws RepositoryException
	{
		try {
			if(!(sail instanceof AdvancedSail) || !((AdvancedSail)sail).isInitialized()) {
				sail.initialize();
			}
		}
		catch (SailLockedException e) {
			String l = e.getLockedBy();
			String r = e.getRequestedBy();
			String m = e.getMessage();
			throw new RepositoryLockedException(l, r, m, e);
		}
		catch (SailException e) {
			throw new RepositoryException(e.getMessage(), e);
		}
	}

	@Override
	protected void shutDownInternal()
		throws RepositoryException
	{
		try {
			sail.shutDown();
		}
		catch (SailException e) {
			throw new RepositoryException("Unable to shutdown Sail", e);
		}
	}

	/**
	 * Gets the Sail object that is on top of the Sail stack that this repository
	 * operates on.
	 * 
	 * @return A Sail object.
	 */
	public Sail getSail() {
		return sail;
	}

	public boolean isWritable()
		throws RepositoryException
	{
		try {
			return sail.isWritable();
		}
		catch (SailException e) {
			throw new RepositoryException("Unable to determine writable status of Sail", e);
		}
	}

	public ValueFactory getValueFactory() {
		return sail.getValueFactory();
	}

	public SailRepositoryConnection getConnection()
		throws RepositoryException
	{
		try {
			return new SailRepositoryConnection(this, sail.getConnection());
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	public String toString() {
		return sail.toString();
	}

	private <T> T findSailOf(Sail sail, Class<T> type) {
		if (type.isInstance(sail)) {
			return type.cast(sail);
		}
		else if (sail instanceof StackableSail) {
			return findSailOf(((StackableSail)sail).getBaseSail(), type);
		}
		else {
			return null;
		}
	}
}
