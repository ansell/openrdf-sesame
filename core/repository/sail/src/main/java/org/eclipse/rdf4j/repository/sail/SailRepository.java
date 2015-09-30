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

import java.io.File;

import org.apache.http.client.HttpClient;
import org.eclipse.rdf4j.http.client.HttpClientDependent;
import org.eclipse.rdf4j.http.client.SesameClient;
import org.eclipse.rdf4j.http.client.SesameClientDependent;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolver;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolverClient;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryLockedException;
import org.eclipse.rdf4j.repository.base.AbstractRepository;
import org.eclipse.rdf4j.repository.sail.config.RepositoryResolver;
import org.eclipse.rdf4j.repository.sail.config.RepositoryResolverClient;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.SailLockedException;
import org.eclipse.rdf4j.sail.StackableSail;

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
public class SailRepository extends AbstractRepository implements FederatedServiceResolverClient,
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
			sail.initialize();
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
