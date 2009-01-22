/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.dataset;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailMetaData;
import org.openrdf.sail.helpers.SailMetaDataWrapper;
import org.openrdf.sail.helpers.SailWrapper;
import org.openrdf.store.StoreException;

/**
 * Loads and refreshes all the default graphs from QueryModel in a cleared
 * context of the same URL. Loads and refreshes all the registered named graphs
 * from QueryModel in a cleared context of the named URI.
 * 
 * @author James Leigh
 */
public class DatasetSail extends SailWrapper {

	private ClassLoader cl;

	private boolean closed;

	private Map<URI, String> graphs = new HashMap<URI, String>();

	private Map<URL, Long> lastModified = new ConcurrentHashMap<URL, Long>();

	private SailRepository repository;

	private String accept;

	public DatasetSail() {
		super();
	}

	public DatasetSail(Sail delegate) {
		super(delegate);
	}

	private ClassLoader getClassLoader() {
		if (cl == null)
			return Thread.currentThread().getContextClassLoader();
		return cl;
	}

	public void setClassLoader(ClassLoader cl) {
		this.cl = cl;
	}

	/** These are the only datasets that this repository can load. */
	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	/**
	 * null dataset indicates the graph is known, but will be loaded externally.
	 */
	public void addGraph(URI name, String dataset) {
		graphs.put(name, dataset);
	}

	public void setGraphs(Map<URI, String> graphs) {
		this.graphs = new HashMap<URI, String>(graphs);
	}

	public Map<URI, String> getGraphs() {
		return graphs;
	}

	@Override
	public SailMetaData getMetaData()
		throws StoreException
	{
		return new SailMetaDataWrapper(super.getMetaData()) {

			@Override
			public boolean isRemoteDatasetSupported() {
				return true;
			}
		};
	}

	@Override
	public void initialize()
		throws StoreException
	{
		repository = new SailRepository(getDelegate());
		repository.initialize();
		for (URI graph : graphs.keySet()) {
			loadGraph(graph);
		}
		accept = getAcceptHeader();
	}

	@Override
	public void shutDown()
		throws StoreException
	{
		repository.shutDown();
	}

	@Override
	public SailConnection getConnection()
		throws StoreException
	{
		return new DatasetConnection(this, getDelegate().getConnection());
	}

	public void loadGraph(URI graph)
		throws StoreException
	{
		if (graphs.containsKey(graph)) {
			loadDataset(graphs.get(graph), graph);
		}
		else if (!closed) {
			loadDataset(graph.stringValue(), graph);
		}
	}

	public void loadDataset(String path, URI context)
		throws StoreException
	{
		try {
			if (path == null || closed && !path.equals(graphs.get(context)))
				return;
			URL url = findURL(path);
			Long since = lastModified.get(url);
			URLConnection urlCon = url.openConnection();
			if (since != null) {
				urlCon.setIfModifiedSince(since);
			}
			if (accept != null) {
				urlCon.setRequestProperty("Accept", accept);
			}
			if (since == null || since < urlCon.getLastModified()) {
				load(url, urlCon, context);
			}
		}
		catch (RDFParseException e) {
			throw new StoreException(e);
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
	}
	private String getAcceptHeader() {
		StringBuilder sb = new StringBuilder();
		String preferred = RDFFormat.RDFXML.getDefaultMIMEType();
		sb.append(preferred).append(";q=0.2");
		Set<RDFFormat> rdfFormats = RDFParserRegistry.getInstance().getKeys();
		for (RDFFormat format : rdfFormats) {
			for (String type : format.getMIMETypes()) {
				if (!preferred.equals(type)) {
					sb.append(", ").append(type);
				}
			}
		}
		return sb.toString();
	}

	private URL findURL(String path)
		throws MalformedURLException
	{
		if (path.contains(":/"))
			return new URL(path);
		return getClassLoader().getResource(path);
	}

	private synchronized void load(URL url, URLConnection urlCon, URI context)
		throws StoreException, RDFParseException, IOException
	{
		long modified = urlCon.getLastModified();
		if (lastModified.containsKey(url) && lastModified.get(url) >= modified) {
			return;
		}

		// Try to determine the data's MIME type
		RDFFormat format = null;
		String mimeType = urlCon.getContentType();
		if (mimeType != null) {
			int semiColonIdx = mimeType.indexOf(';');
			if (semiColonIdx >= 0) {
				mimeType = mimeType.substring(0, semiColonIdx);
			}
			format = RDFParserRegistry.getInstance().getFileFormatForMIMEType(mimeType);
		}

		// Fall back to using file name extensions
		if (format == null) {
			format = RDFParserRegistry.getInstance().getFileFormatForFileName(url.getFile(), RDFFormat.RDFXML);
		}

		InputStream stream = urlCon.getInputStream();
		try {
			RepositoryConnection repCon = getRepositoryConnection();
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

	private RepositoryConnection getRepositoryConnection()
		throws StoreException
	{
		return repository.getConnection();
	}
}
