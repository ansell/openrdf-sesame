/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.openrdf.repository.DelegatingRepository;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.DelegatingRepositoryImplConfig;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryConfigUtil;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryRegistry;
import org.openrdf.repository.event.base.RepositoryConnectionListenerAdapter;

/**
 * An implementation of the {@link RepositoryManager} interface that operates
 * directly on the repository data files in the local file system.
 * 
 * @author Arjohn Kampman
 */
public class LocalRepositoryManager extends RepositoryManager {

	/*-----------*
	 * Constants *
	 *-----------*/

	public static final String REPOSITORIES_DIR = "repositories";

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The base dir to resolve any relative paths against.
	 */
	private File baseDir;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new RepositoryManager that operates on the specfified base
	 * directory.
	 * 
	 * @param baseDir
	 *        The base directory where data for repositories can be stored, among
	 *        other things.
	 */
	public LocalRepositoryManager(File baseDir) {
		super();

		this.baseDir = baseDir;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected SystemRepository createSystemRepository()
		throws RepositoryException
	{
		File systemDir = getRepositoryDir(SystemRepository.ID);
		SystemRepository systemRepos = new SystemRepository(systemDir);
		systemRepos.initialize();

		systemRepos.addRepositoryConnectionListener(new ConfigChangeListener());
		return systemRepos;
	}

	/**
	 * Gets the base dir against which to resolve relative paths.
	 */
	public File getBaseDir() {
		return baseDir;
	}

	/**
	 * Resolves the specified path against the manager's base directory.
	 * 
	 * @see #getDataDir
	 */
	public File resolvePath(String path) {
		return new File(getBaseDir(), path);
	}

	public File getRepositoryDir(String repositoryID) {
		File repositoriesDir = resolvePath(REPOSITORIES_DIR);
		return new File(repositoriesDir, repositoryID);
	}

	@Override
	public SystemRepository getSystemRepository() {
		return (SystemRepository)super.getSystemRepository();
	}

	@Override
	protected Repository createRepository(String id)
		throws RepositoryConfigException, RepositoryException
	{
		Repository systemRepository = getSystemRepository();

		RepositoryConnection con = systemRepository.getConnection();
		try {
			Repository repository = null;

			RepositoryConfig repConfig = RepositoryConfigUtil.getRepositoryConfig(systemRepository, id);
			if (repConfig != null) {
				repConfig.validate();

				repository = createRepositoryStack(repConfig.getRepositoryImplConfig());
				repository.setDataDir(getRepositoryDir(id));
				repository.initialize();
			}

			return repository;
		}
		finally {
			con.close();
		}
	}

	/**
	 * Creates the stack of Repository objects for the repository represented by
	 * the specified <tt>repositoryImplNode</tt>.
	 * 
	 * @param con
	 *        A connection to the repository containing the repository
	 *        configuration.
	 * @param repositoryImplNode
	 *        The node representing the to-be-created repository in the
	 *        configuration.
	 * @return The created repository, or <tt>null</tt> if no such repository
	 *         exists.
	 * @throws RepositoryConfigException
	 *         If no repository could be created due to invalid or incomplete
	 *         configuration data.
	 */
	private Repository createRepositoryStack(RepositoryImplConfig config)
		throws RepositoryConfigException
	{
		RepositoryFactory factory = RepositoryRegistry.getInstance().get(config.getType());
		if (factory == null) {
			throw new RepositoryConfigException("Unsupported repository type: " + config.getType());
		}

		Repository repository = factory.getRepository(config);

		if (config instanceof DelegatingRepositoryImplConfig) {
			RepositoryImplConfig delegateConfig = ((DelegatingRepositoryImplConfig)config).getDelegate();

			Repository delegate = createRepositoryStack(delegateConfig);

			try {
				((DelegatingRepository)repository).setDelegate(delegate);
			}
			catch (ClassCastException e) {
				throw new RepositoryConfigException(
						"Delegate specified for repository that is not a DelegatingRepository: "
								+ delegate.getClass());
			}
		}

		return repository;
	}

	@Override
	protected Map<String, RepositoryInfo> createRepositoryInfos()
		throws RepositoryException
	{
		Map<String, RepositoryInfo> result = new TreeMap<String, RepositoryInfo>();

		try {
			Set<String> ids = getRepositoryIDs();

			for (String id : ids) {
				RepositoryConfig config = null;
				if (id.equals(SystemRepository.ID)) {
					config = new RepositoryConfig(id, new SystemRepositoryConfig());
				}
				else {
					config = getRepositoryConfig(id);
				}

				RepositoryInfo repInfo = new RepositoryInfo();
				repInfo.setId(id);
				repInfo.setDescription(config.getTitle());
				try {
					repInfo.setLocation(getRepositoryDir(id).toURI().toURL());
				}
				catch (MalformedURLException mue) {
					throw new RepositoryException("Location of repository does not resolve to a valid URL", mue);
				}

				repInfo.setType(config.getRepositoryImplConfig().getType());

				repInfo.setReadable(true);
				repInfo.setWritable(true);

				result.put(id, repInfo);
			}
		}
		catch (RepositoryConfigException rce) {
			throw new RepositoryException("Unable to retrieve existing configurations", rce);
		}

		return result;
	}

	class ConfigChangeListener extends RepositoryConnectionListenerAdapter {

		@Override
		public void commit(RepositoryConnection con) {
			refresh();
		}
	}
}
