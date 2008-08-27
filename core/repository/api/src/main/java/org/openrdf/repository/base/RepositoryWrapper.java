/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.base;

import java.io.File;

import org.openrdf.StoreException;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.DelegatingRepository;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

/**
 * A {@link DelegatingRepository} implementation that, by default, forwards all
 * method calls to its delegate.
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
 */
public class RepositoryWrapper implements DelegatingRepository {

	private Repository delegate;

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
		throws StoreException
	{
		getDelegate().initialize();
	}

	public void shutDown()
		throws StoreException
	{
		getDelegate().shutDown();
	}

	public boolean isWritable()
		throws StoreException
	{
		return getDelegate().isWritable();
	}

	public RepositoryConnection getConnection()
		throws StoreException
	{
		return getDelegate().getConnection();
	}

	public ValueFactory getValueFactory() {
		return getDelegate().getValueFactory();
	}
}
