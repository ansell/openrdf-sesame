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
import org.openrdf.rio.RDFParserRegistry;

public class DatasetRepository extends RepositoryWrapper {

	private Map<URL, Long> lastModified = new ConcurrentHashMap<URL, Long>();

	public DatasetRepository() {
		super();
	}

	public DatasetRepository(SailRepository delegate) {
		super(delegate);
	}

	@Override
	public void setDelegate(Repository delegate) {
		if (delegate instanceof SailRepository) {
			super.setDelegate(delegate);
		}
		else {
			throw new IllegalArgumentException("delegate must be a SailRepository, is: " + delegate.getClass());
		}
	}

	@Override
	public SailRepository getDelegate() {
		return (SailRepository)super.getDelegate();
	}

	@Override
	public RepositoryConnection getConnection()
		throws RepositoryException
	{
		return new DatasetRepositoryConnection(this, getDelegate().getConnection());
	}

	public void loadDataset(URL url, URI context)
		throws RepositoryException
	{
		try {
			Long since = lastModified.get(url);
			URLConnection urlCon = url.openConnection();
			if (since != null) {
				urlCon.setIfModifiedSince(since);
			}
			if (since == null || since < urlCon.getLastModified()) {
				load(url, urlCon, context);
			}
		}
		catch (RDFParseException e) {
			throw new RepositoryException(e);
		}
		catch (IOException e) {
			throw new RepositoryException(e);
		}
	}

	private synchronized void load(URL url, URLConnection urlCon, URI context)
		throws RepositoryException, RDFParseException, IOException
	{
		long modified = urlCon.getLastModified();
		if (lastModified.containsKey(url) && lastModified.get(url) >= modified) {
			return;
		}

		// Try to determine the data's MIME type
		String mimeType = urlCon.getContentType();
		int semiColonIdx = mimeType.indexOf(';');
		if (semiColonIdx >= 0) {
			mimeType = mimeType.substring(0, semiColonIdx);
		}
		RDFFormat format = RDFParserRegistry.getInstance().getFileFormatForMIMEType(mimeType);

		// Fall back to using file name extensions
		if (format == null) {
			format = RDFParserRegistry.getInstance().getFileFormatForFileName(url.getPath());
		}

		InputStream stream = urlCon.getInputStream();
		try {
			RepositoryConnection repCon = super.getConnection();
			try {
				repCon.setAutoCommit(false);
				repCon.clear(context);
				repCon.add(stream, url.toExternalForm(), format, context);
				repCon.commit();
				lastModified.put(url, modified);
			}
			finally {
				repCon.close();
			}
		}
		finally {
			stream.close();
		}
	}
}
