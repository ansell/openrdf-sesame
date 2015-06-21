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
package org.openrdf.repository.base;

import java.io.File;

import org.openrdf.model.ValueFactory;
import org.openrdf.repository.DelegatingRepository;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * A {@link DelegatingRepository} implementation that, by default, forwards all
 * method calls to its delegate.
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
 */
public class RepositoryWrapper implements DelegatingRepository {

	private volatile Repository delegate;

	/**
	 * Creates a new <tt>RepositoryWrapper</tt>.
	 */
	public RepositoryWrapper() {
	}

	/**
	 * Creates a new <tt>RepositoryWrapper</tt> and calls
	 * {@link #setDelegate(Repository)} with the supplied delegate repository.
	 */
	public RepositoryWrapper(Repository delegate) {
		setDelegate(delegate);
	}

	public void setDelegate(Repository delegate) {
		this.delegate = delegate;
	}

	public Repository getDelegate() {
		return delegate;
	}

	public void setDataDir(File dataDir) {
		getDelegate().setDataDir(dataDir);
	}

	public File getDataDir() {
		return getDelegate().getDataDir();
	}

	public void initialize()
		throws RepositoryException
	{
		getDelegate().initialize();
	}

	public void shutDown()
		throws RepositoryException
	{
		getDelegate().shutDown();
	}

	public boolean isWritable()
		throws RepositoryException
	{
		return getDelegate().isWritable();
	}

	public RepositoryConnection getConnection()
		throws RepositoryException
	{
		return getDelegate().getConnection();
	}

	public ValueFactory getValueFactory() {
		return getDelegate().getValueFactory();
	}

	public String toString() {
		return getDelegate().toString();
	}

	public boolean isInitialized() {
		return getDelegate().isInitialized();
	}
}
