/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.dataset;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.base.RepositoryWrapper;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

public class DatasetRepository extends RepositoryWrapper {

	private SailRepository delegate;

	private Map<URL, Long> lastModified = new ConcurrentHashMap<URL, Long>();

	public DatasetRepository() {
		super();
	}

	public DatasetRepository(SailRepository delegate) {
		super(delegate);
		this.delegate = delegate;
	}

	@Override
	public void setDelegate(Repository delegate) {
		assert delegate instanceof SailRepository : delegate.getClass();
		super.setDelegate(delegate);
		this.delegate = (SailRepository)delegate;
	}

	@Override
	public RepositoryConnection getConnection()
		throws RepositoryException
	{
		return new DatasetRepositoryConnection(this, delegate.getConnection());
	}

	public void loadDataset(URL url, URI context)
		throws RepositoryException
	{
		try {
			Long since = lastModified.get(url);
			URLConnection open = url.openConnection();
			if (since != null) {
				open.setIfModifiedSince(since);
			}
			if (since == null || since < open.getLastModified()) {
				load(url, open, context);
			}
		}
		catch (RDFParseException e) {
			throw new RepositoryException(e);
		}
		catch (IOException e) {
			throw new RepositoryException(e);
		}
	}

	private synchronized void load(URL url, URLConnection open, URI context)
		throws RepositoryException, RDFParseException, IOException
	{
		long modified = open.getLastModified();
		if (lastModified.containsKey(url) && lastModified.get(url) >= modified) {
			return;
		}
		RDFFormat format = RDFFormat.forMIMEType(open.getContentType());
		if (format == null) {
			format = RDFFormat.forFileName(url.getFile());
		}
		RepositoryConnection conn = null;
		InputStream stream = open.getInputStream();
		try {
			conn = super.getConnection();
			conn.setAutoCommit(false);
			conn.clear(context);
			conn.add(stream, url.toExternalForm(), format, context);
			conn.commit();
			lastModified.put(url, modified);
		}
		finally {
			stream.close();
			if (conn != null) {
				conn.close();
			}
		}
	}

}
