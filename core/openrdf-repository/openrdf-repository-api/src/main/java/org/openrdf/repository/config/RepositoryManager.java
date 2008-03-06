/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.config;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.Sail;
import org.openrdf.sail.StackableSail;

@Deprecated
public class RepositoryManager {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	/*--------------*
	 * Static stuff *
	 *--------------*/

	/**
	 * The default RepositoryManager object. In the future, multiple
	 * RepositoryManager instances might co-exist, for example to reflect virtual
	 * hosts.
	 */
	private static final RepositoryManager DEFAULT_INSTANCE = new RepositoryManager();

	/**
	 * Gets the default manager object.
	 */
	public static RepositoryManager getDefaultInstance() {
		return DEFAULT_INSTANCE;
	}

	/**
	 * The base dir to resolve any relative paths against.
	 */
	private static File _dataDir = null;

	/*-----------*
	 * Variables *
	 *-----------*/

	private Map<String, Repository> _repositories;

	private RepositoryManagerConfig _config;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public RepositoryManager() {
		_repositories = new HashMap<String, Repository>();
		_config = new RepositoryManagerConfig();
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Sets the data dir against which relative paths used by Sesame are
	 * resolved. If not set, Sesame leaves the path resolving to Java, which
	 * usually means that paths are resolved against the directory where the
	 * application was started.
	 */
	public static final void setDataDir(File dataDir) {
		_dataDir = dataDir;
	}

	/**
	 * Gets the base dir against which to resolve relative paths (null if not
	 * set).
	 */
	public static final File getDataDir() {
		return _dataDir;
	}

	/**
	 * Resolves the specified path to the file that it refers to. Relative paths
	 * are resolved against the Sesame base directory, if set.
	 * 
	 * @see #setBaseDir
	 */
	public static File resolvePath(String path) {
		return new File(getDataDir(), path);
	}

	/**
	 * Sets the configuration for this manager. The configuration specifies which
	 * repositories are available. Supplying a new configuration will replace any
	 * exisiting configuration.
	 * 
	 * @param newConfig
	 *        The (new) configuration.
	 */
	public void setConfig(RepositoryManagerConfig newConfig) {
		synchronized (_repositories) {
			Map<String, Repository> newRepositories = new HashMap<String, Repository>();
			Map<String, Repository> oldRepositories = new HashMap<String, Repository>(_repositories);

			for (RepositoryConfig newRC : newConfig.getRepositoryConfigs()) {
				String id = newRC.getID();

				// If the 'new' RepositoryConfig hasn't changed, then reuse the
				// old Repository object
				RepositoryConfig oldRC = _config.getRepositoryConfig(id);
				if (oldRC != null && newRC.equals(oldRC)) {
					Repository repository = oldRepositories.remove(id);
					if (repository != null) {
						newRepositories.put(id, repository);
					}
				}
			}

			_config = newConfig;
			_repositories = newRepositories;

			// Shut down all changed or removed repositories
			for (Repository repository : oldRepositories.values()) {
				try {
					repository.shutDown();
				}
				catch (RepositoryException e) {
					logger.error("Repository shut down failed", e);
				}
			}
		}
	}

	/**
	 * Gets the configuration for this manager.
	 * 
	 * @return The RepositoryManagerConfig object used by this manager.
	 */
	public RepositoryManagerConfig getConfig() {
		return _config;
	}

	/**
	 * Gets the repository that is known by the specified ID from this manager.
	 * 
	 * @param id
	 *        A repository ID.
	 * @return A Repository object, or <tt>null</tt> if no repository was known
	 *         for the specified ID.
	 */
	public Repository getRepository(String id)
		throws RepositoryManagerConfigException
	{
		synchronized (_repositories) {
			Repository repository = _repositories.get(id);

			if (repository == null) {
				// First call, create the repository.
				synchronized (_config) {
					RepositoryConfig rc = _config.getRepositoryConfig(id);

					if (rc != null) {
						repository = _createRepository(rc);
						_repositories.put(id, repository);
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

			Repository repository = null;
			String className = rc.getClassName();
			if (className == null) {
				className = "org.openrdf.repository.sail.SailRepository";
			}

			Class<?> rclass = Class.forName(className);
			if (sailStack == null) {
				Constructor<?> defaultConstructor = rclass.getConstructor();
				repository = (Repository)defaultConstructor.newInstance();
			}
			else {
				Constructor<?> sailConstructor = rclass.getConstructor(new Class[] { Sail.class });
				repository = (Repository)sailConstructor.newInstance(new Object[] { sailStack });
			}

			repository.setDataDir(new File(getDataDir(), rc.getID()));
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
		catch (RepositoryException e) {
			throw new RepositoryManagerConfigException(e);
		}
		catch (SecurityException e) {
			throw new RepositoryManagerConfigException(e);
		}
		catch (IllegalArgumentException e) {
			throw new RepositoryManagerConfigException(e);
		}
		catch (InvocationTargetException e) {
			throw new RepositoryManagerConfigException(e);
		}
		catch (NoSuchMethodException e) {
			throw new RepositoryManagerConfigException(e);
		}
	}

	/**
	 * Clears the server's configuration and shuts down all repositories that
	 * have been initialized by it.
	 */
	public void clear() {
		synchronized (_repositories) {
			_config = new RepositoryManagerConfig();

			for (Repository repository : _repositories.values()) {
				try {
					repository.shutDown();
				}
				catch (RepositoryException e) {
					logger.error("Repository shut down failed", e);
				}
			}

			_repositories = new HashMap<String, Repository>();
		}
	}
}
