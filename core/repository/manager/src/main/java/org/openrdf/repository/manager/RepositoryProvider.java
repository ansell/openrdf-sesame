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
package org.openrdf.repository.manager;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;

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
			throws RepositoryConfigException, RepositoryException
		{
			if (manager == null || !manager.isInitialized()) {
				shutDown();
				RepositoryManager m = createRepositoryManager(url);
				m.initialize();
				manager = m;
			}
			return manager;
		}

		public synchronized boolean isInitialized() {
			return manager != null && manager.isInitialized();
		}

		public synchronized void shutDown() {
			if (manager != null) {
				manager.shutDown();
			}
		}
	}

	static final Map<String, SynchronizedManager> managers = new HashMap<String, SynchronizedManager>();

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
		throws RepositoryConfigException, RepositoryException
	{
		String uri = normalizeDirectory(url);
		SynchronizedManager sync = null;
		synchronized (managers) {
			Iterator<SynchronizedManager> iter = managers.values().iterator();
			while (iter.hasNext()) {
				SynchronizedManager sm = iter.next();
				if (!sm.isInitialized()) {
					sm.shutDown();
					iter.remove();
				}
			}
			if (managers.containsKey(uri)) {
				sync = managers.get(uri);
			}
			else {
				managers.put(uri, sync = new SynchronizedManager(url));
			}
		}
		return sync.get();
	}

	/**
	 * Creates a LocalRepositoryManager, if not already created, that will be
	 * shutdown when the JVM exits cleanly.
	 */
	public static LocalRepositoryManager getRepositoryManager(File dir)
			throws RepositoryConfigException, RepositoryException
	{
		String url = dir.toURI().toASCIIString();
		return (LocalRepositoryManager) getRepositoryManager(url);
	}

	/**
	 * Returns the RepositoryManager that will be used for the given repository
	 * URL. Creates a RepositoryManager, if not already created, that will be
	 * shutdown when the JVM exits cleanly. The parameter must be a URL of the
	 * form http://host:port/path/repositories/id or file:///path/repositories/id.
	 */
	public static RepositoryManager getRepositoryManagerOfRepository(String url)
			throws RepositoryConfigException, RepositoryException
	{
		if (!url.contains(REPOSITORIES)) {
			throw new IllegalArgumentException("URL is not repository URL: "
					+ url);
		}
		int idx = url.lastIndexOf(REPOSITORIES);
		String server = url.substring(0, idx);
		if (server.length() == 0) {
			server = ".";
		}
		return getRepositoryManager(server);
	}

	/**
	 * Returns the Repository ID that will be passed to a RepositoryManager for the given repository
	 * URL. The parameter must be a URL of the
	 * form http://host:port/path/repositories/id or file:///path/repositories/id.
	 */
	public static String getRepositoryIdOfRepository(String url)
	{
		if (!url.contains(REPOSITORIES)) {
			throw new IllegalArgumentException("URL is not repository URL: "
					+ url);
		}
		int idx = url.lastIndexOf(REPOSITORIES);
		String id = url.substring(idx + REPOSITORIES.length());
		if (id.endsWith("/")) {
			id = id.substring(0, id.length() - 1);
		}
		return id;
	}

	/**
	 * Created a Repository, if not already created, that will be shutdown when
	 * the JVM exits cleanly. The parameter must be a URL of the form
	 * http://host:port/path/repositories/id or file:///path/repositories/id.
	 * @return Repository from a RepositoryManager or null if repository is not defined
	 */
	public static Repository getRepository(String url)
		throws RepositoryException, RepositoryConfigException
	{
		RepositoryManager manager = getRepositoryManagerOfRepository(url);
		String id = getRepositoryIdOfRepository(url);
		return manager.getRepository(id);
	}

	static RepositoryManager createRepositoryManager(String url)
		throws RepositoryConfigException
	{
		if (url.startsWith("http")) {
			return new RemoteRepositoryManager(url);
		}
		else {
			return new LocalRepositoryManager(asLocalFile(url));
		}
	}

	private static File asLocalFile(String url)
		throws RepositoryConfigException
	{
		URI uri = new File(".").toURI().resolve(url);
		return new File(uri);
	}

	private static String normalizeDirectory(String url) throws IllegalArgumentException {
		try {
			if (!url.endsWith("/")) {
				return normalizeDirectory(url + '/');
			}
			URI norm = URI.create(url);
			if (!norm.isAbsolute()) {
				norm = new File(".").toURI().resolve(url);
			}
			norm = norm.normalize();
			if (norm.isOpaque())
				throw new IllegalArgumentException(
						"Repository Manager URL must not be opaque: " + url);
			String sch = norm.getScheme();
			String host = norm.getAuthority();
			String path = norm.getPath();
			if (sch != null) {
				sch = sch.toLowerCase();
			}
			if (host != null) {
				host = host.toLowerCase();
			}
			return new URI(sch, host, path, null, null).toASCIIString();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

}
