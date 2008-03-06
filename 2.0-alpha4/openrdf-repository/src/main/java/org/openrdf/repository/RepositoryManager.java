/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryManagerConfig;
import org.openrdf.repository.config.RepositoryManagerConfigException;
import org.openrdf.repository.config.SailConfig;
import org.openrdf.repository.config.SailParameter;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.SailInitializationException;
import org.openrdf.sail.StackableSail;
import org.openrdf.util.log.ThreadLog;


/**
 * A Sesame server that manages repositories and their access permissions.
 */
public class RepositoryManager {

	/*--------------*
	 * Static stuff *
	 *--------------*/

	/**
	 * The default server object. In the future, multiple Server instances might
	 * co-exists, for example to reflect virtual hosts.
	 */
	private static final RepositoryManager DEFAULT_SERVER = new RepositoryManager();

	/**
	 * Gets the default server object.
	 */
	public static RepositoryManager getDefaultServer() {
		return DEFAULT_SERVER;
	}


	/**
	 * The base dir to resolve any relative paths against.
	 */
	private static File _baseDir = null;
	
	/*-----------*
	 * Variables *
	 *-----------*/

	private Map<String, Repository> _repositoryMap;

	private RepositoryManagerConfig _serverConfig;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public RepositoryManager() {
		_repositoryMap = new HashMap<String, Repository>();
		_serverConfig = new RepositoryManagerConfig();
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Sets the base dir against which relative paths used by Sesame are
	 * resolved. If not set, Sesame leaves the path resolving to Java, which
	 * usually means that paths are resolved against the directory where the
	 * application was started.
	 **/
	public static final void setBaseDir(File baseDir) {
		_baseDir = baseDir;
	}

	/**
	 * Gets the base dir against which to resolve relative paths (null if not
	 * set).
	 **/
	public static final File getBaseDir() {
		return _baseDir;
	}

	/**
	 * Resolves the specified path to the file that it refers to. Relative paths
	 * are resolved against the Sesame base directory, if set.
	 *
	 * @see #setBaseDir
	 */
	public static File resolvePath(String path) {
		return new File(getBaseDir(), path);
	}
	
	/**
	 * Sets the server configuration for this server. The configuration specifies
	 * which repositories are available on this server and who has access to
	 * them. Supplying a new server configuration will replace any exisiting
	 * configuration.
	 * 
	 * @param newConfig
	 *        The (new) server configuration.
	 */
	public void setServerConfig(RepositoryManagerConfig newConfig) {
		synchronized (_repositoryMap) {
			Map<String, Repository> newRepositoryMap = new HashMap<String, Repository>();
			Map<String, Repository> oldRepositoryMap = new HashMap<String, Repository>(_repositoryMap);

			for (RepositoryConfig newRC : newConfig.getRepositoryConfigs()) {
				String id = newRC.getID();

				// If the 'new' RepositoryConfig hasn't changed, then reuse the
				// old Repository object
				RepositoryConfig oldRC = _serverConfig.getRepositoryConfig(id);
				if (oldRC != null && newRC.equals(oldRC)) {
					Repository repository = oldRepositoryMap.remove(id);
					if (repository != null) {
						newRepositoryMap.put(id, repository);
					}
				}
			}

			_serverConfig = newConfig;
			_repositoryMap = newRepositoryMap;

			// Shut down all changed or removed repositories
			for (Repository repository : oldRepositoryMap.values()) {
				try {
					repository.shutDown();
				}
				catch (SailException e) {
					ThreadLog.error("Repository shut down failed", e);
				}
			}
		}
	}

	/**
	 * Gets the server configuration for this server.
	 * 
	 * @return The ServerConfig object used by this server.
	 */
	public RepositoryManagerConfig getServerConfig() {
		return _serverConfig;
	}

	/**
	 * Gets the repository that is known by the specified ID from this server.
	 * 
	 * @param id
	 *        A repository ID.
	 * @return A Repository object, or <tt>null</tt> if no repository was known
	 *         for the specified ID.
	 */
	public Repository getRepository(String id)
		throws RepositoryManagerConfigException
	{
		synchronized (_repositoryMap) {
			Repository repository = _repositoryMap.get(id);

			if (repository == null) {
				// First call, create the repository.
				synchronized (_serverConfig) {
					RepositoryConfig rc = _serverConfig.getRepositoryConfig(id);

					if (rc != null) {
						repository = _createRepository(rc);
						_repositoryMap.put(id, repository);
					}
				}
			}

			return repository;
		}
	}

	/**
	 * Creates a repository based on the supplied repository configuration
	 * parameters.
	 * 
	 * @param rc
	 *        The repository configuration parameters.
	 * @return
	 * @throws RepositoryManagerConfigException
	 */
	private Repository _createRepository(RepositoryConfig rc)
		throws RepositoryManagerConfigException
	{
		try {
			// Create and initialize the Sail stack
			Sail sailStack = null;

			for (SailConfig sailConfig : rc.getSailConfigStack()) {
				String className = sailConfig.getClassName();
				Class<?> sailClass = Class.forName(className);

				Sail sail = null;
				try {
					sail = (Sail)sailClass.newInstance();
				}
				catch (ClassCastException e) {
					throw new RepositoryManagerConfigException(sailClass + " does not implement Sail interface.");
				}

				if (sailStack != null) {
					// Stack this Sail on top of the existing Sail stack
					try {
						((StackableSail)sail).setBaseSail(sailStack);
					}
					catch (ClassCastException e) {
						throw new RepositoryManagerConfigException(className + " is not a StackableSail.");
					}
				}

				for (SailParameter param : sailConfig.getParameters()) {
					sail.setParameter(param.getName(), param.getValue());
				}

				sailStack = sail;
			}

			if (sailStack == null) {
				throw new RepositoryManagerConfigException("No Sail stack has been specified");
			}

			Repository repository;
			String className = rc.getClassName();
			if (className == null) {
				repository = new RepositoryImpl(sailStack);
			} else {
				Class rclass = Class.forName(className);
				Constructor constructor = rclass.getConstructor(new Class[]{Sail.class});
				repository = (Repository)constructor.newInstance(new Object[]{sailStack});
			}
			repository.initialize();
			return repository;
		}
		catch (ClassNotFoundException e) {
			throw new RepositoryManagerConfigException(e);
		}
		catch (InstantiationException e) {
			throw new RepositoryManagerConfigException(e);
		}
		catch (IllegalAccessException e) {
			throw new RepositoryManagerConfigException(e);
		}
		catch (SailInitializationException e) {
			throw new RepositoryManagerConfigException(e);
		} catch (SecurityException e) {
			throw new RepositoryManagerConfigException(e);
		} catch (NoSuchMethodException e) {
			throw new RepositoryManagerConfigException(e);
		} catch (IllegalArgumentException e) {
			throw new RepositoryManagerConfigException(e);
		} catch (InvocationTargetException e) {
			throw new RepositoryManagerConfigException(e);
		}
	}

	/**
	 * Clears the server's configuration and shuts down all repositories that
	 * have been initialized by it.
	 */
	public void clear() {
		synchronized (_repositoryMap) {
			_serverConfig = new RepositoryManagerConfig();

			for (Repository repository : _repositoryMap.values()) {
				try {
					repository.shutDown();
				}
				catch (SailException e) {
					ThreadLog.error("Repository shut down failed", e);
				}
			}

			_repositoryMap = new HashMap<String, Repository>();
		}
	}
}
