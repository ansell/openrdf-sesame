/*
 * Copyright James Leigh (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.store.StoreConfigException;
import org.openrdf.store.StoreException;

/**
 * A static access point to manage RepositoryManagers that are automatically
 * shutdown when the JVM is closed.
 * 
 * @author James Leigh
 */
public class RepositoryProvider {

	private static final String REPOSITORIES = "repositories/";

	private static class SynchronizedManager {

		private final String url;

		private RepositoryManager manager;

		public SynchronizedManager(String url) {
			this.url = url;
		}

		public synchronized RepositoryManager get()
			throws StoreConfigException
		{
			if (manager == null) {
				RepositoryManager m = createRepositoryManager(url);
				m.initialize();
				manager = m;
			}
			return manager;
		}

		public synchronized void shutDown() {
			if (manager != null) {
				manager.shutDown();
			}
		}
	}

	private static final Map<String, SynchronizedManager> managers = new HashMap<String, SynchronizedManager>();

	static {
		Runtime.getRuntime().addShutdownHook(new Thread("RepositoryProvider-shutdownHook") {

			public void run() {
				synchronized (managers) {
					for (SynchronizedManager manager : managers.values()) {
						manager.shutDown();
					}
				}
			}
		});
	}

	/**
	 * Creates a RepositoryManager, if not already created, that will be shutdown
	 * when the JVM exits cleanly. The parameter must be a URL of the form
	 * http://host:port/path or file:///path.
	 */
	public static RepositoryManager getRepositoryManager(String url)
		throws StoreConfigException
	{
		SynchronizedManager sync = null;
		synchronized (managers) {
			if (managers.containsKey(url)) {
				sync = managers.get(url);
			}
			else {
				managers.put(url, sync = new SynchronizedManager(url));
			}
		}
		return sync.get();
	}

	/**
	 * Created a Repository, if not already created, that will be shutdown when
	 * the JVM exits cleanly. The parameter must be a URL of the form
	 * http://host:port/path/repositories/id or file:///path/repositories/id.
	 */
	public static Repository getRepository(String url)
		throws StoreException, StoreConfigException
	{
		if (!url.contains(REPOSITORIES)) {
			throw new IllegalArgumentException("URL is not repository URL: " + url);
		}
		int idx = url.lastIndexOf(REPOSITORIES);
		String server = url.substring(0, idx);
		String id = url.substring(idx + REPOSITORIES.length());
		if (server.endsWith("/")) {
			server = server.substring(0, server.length() - 1);
		} else if (server.length() == 0) {
			server = ".";
		}
		if (id.endsWith("/")) {
			id = id.substring(0, id.length() - 1);
		}
		RepositoryManager manager = getRepositoryManager(server);
		return manager.getRepository(id);
	}

	/**
	 * Created a new RepositoryConnection, that must be closed by the caller. The
	 * parameter must be a URL of the form http://host:port/path/repositories/id
	 * or file:///path/repositories/id.
	 */
	public static RepositoryConnection getConnection(String url)
		throws StoreException, StoreConfigException
	{
		Repository repository = getRepository(url);
		return repository.getConnection();
	}

	static RepositoryManager createRepositoryManager(String url)
		throws StoreConfigException
	{
		if (url.startsWith("http")) {
			return new RemoteRepositoryManager(url);
		}
		else {
			return new LocalRepositoryManager(asLocalFile(url));
		}
	}

	private static File asLocalFile(String url)
		throws StoreConfigException
	{
		URI uri = new File(".").toURI().resolve(url);
		return new File(uri);
	}

}
