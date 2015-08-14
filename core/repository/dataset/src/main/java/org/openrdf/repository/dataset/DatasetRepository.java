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
package org.openrdf.repository.dataset;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.model.IRI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.base.RepositoryWrapper;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;

/**
 * A repository that automatically attempts to load the dataset supplied in a
 * (SPARQL) query (using FROM and FROM NAMED clauses).
 *
 * @author Arjohn Kampman
 * @author Jeen Broekstra
 */
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

	/**
	 * Inspects if the dataset at the supplied URL location has been modified
	 * since the last load into this repository and if so loads it into the
	 * supplied context.
	 * 
	 * @param url
	 *        the location of the dataset
	 * @param context
	 *        the context in which to load the dataset
	 * @param config
	 *        parser configuration to use for processing the dataset
	 * @throws RepositoryException
	 *         if an error occurred while loading the dataset.
	 */
	public void loadDataset(URL url, IRI context, ParserConfig config)
		throws RepositoryException
	{
		try {
			Long since = lastModified.get(url);
			URLConnection urlCon = url.openConnection();
			if (since != null) {
				urlCon.setIfModifiedSince(since);
			}
			if (since == null || since < urlCon.getLastModified()) {
				load(url, urlCon, context, config);
			}
		}
		catch (RDFParseException e) {
			throw new RepositoryException(e);
		}
		catch (IOException e) {
			throw new RepositoryException(e);
		}
	}

	private synchronized void load(URL url, URLConnection urlCon, IRI context, ParserConfig config)
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
		RDFFormat format = Rio.getParserFormatForMIMEType(mimeType).orElse(
				Rio.getParserFormatForFileName(url.getPath()).orElseThrow(Rio.unsupportedFormat(mimeType)));

		InputStream stream = urlCon.getInputStream();
		try {
			RepositoryConnection repCon = super.getConnection();
			try {
				repCon.setParserConfig(config);
				repCon.begin();
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
