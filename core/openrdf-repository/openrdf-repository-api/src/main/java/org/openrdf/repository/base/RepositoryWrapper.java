/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.base;

import java.io.File;

import org.openrdf.model.ValueFactory;
import org.openrdf.repository.DelegatingRepository;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;


/**
 *
 * @author Herko ter Horst
 */
public class RepositoryWrapper implements DelegatingRepository {

	private Repository delegate;
	
	public RepositoryWrapper(Repository delegate) {
		this.delegate = delegate;
	}
	
	public Repository getDelegate() {
		return delegate;
	}

	public RepositoryConnection getConnection()
		throws RepositoryException
	{
		return getDelegate().getConnection();
	}

	public File getDataDir() {
		return getDelegate().getDataDir();
	}

	public ValueFactory getValueFactory() {
		return getDelegate().getValueFactory();
	}

	public void initialize()
		throws RepositoryException
	{
		getDelegate().initialize();
	}

	public boolean isWritable()
		throws RepositoryException
	{
		return getDelegate().isWritable();
	}

	public void setDataDir(File dataDir) {
		getDelegate().setDataDir(dataDir);
	}

	public void shutDown()
		throws RepositoryException
	{
		getDelegate().shutDown();
	}
}
