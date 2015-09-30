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
