/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.Literal;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
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
 * A manager for {@link Repository}s. Every <tt>RepositoryManager</tt> has
 * one SYSTEM repository and zero or more "user repositories". The SYSTEM
 * repository contains data that describes the configuration of the other
 * repositories (their IDs, which implementations of the Repository API to use,
 * access rights, etc.). The other repositories are instantiated based on this
 * configuration data.
 */
public class RepositoryManager {

	/*-----------*
	 * Constants *
	 *-----------*/

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final String REPOSITORIES_DIR = "repositories";

	private static final String ALL_REPOSITORIES_QUERY;

	static {
		StringBuilder query = new StringBuilder(256);
		query.setLength(0);
		query.append("SELECT ID ");
		query.append("FROM {} rdf:type {sys:Repository};");
		query.append("        sys:repositoryID {ID} ");
		query.append("WHERE isLiteral(ID) ");
		query.append("USING NAMESPACE sys = <http://www.openrdf.org/config/repository#>");
		ALL_REPOSITORIES_QUERY = query.toString();
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The base dir to resolve any relative paths against.
	 */
	private File baseDir;

	private Map<String, Repository> repositories;

	private RepositoryRegistry repositoryRegistry;

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
	public RepositoryManager(File baseDir)
		throws RepositoryException
	{
		this.baseDir = baseDir;
		this.repositories = new HashMap<String, Repository>();
		this.repositoryRegistry = RepositoryRegistry.getInstance();

		initSystemRepository();
	}

	/*---------*
	 * Methods *
	 *---------*/

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

	private void initSystemRepository()
		throws RepositoryException
	{
		File systemDir = getRepositoryDir(SystemRepository.ID);
		SystemRepository systemRepos = new SystemRepository(systemDir);
		systemRepos.initialize();

		systemRepos.addRepositoryConnectionListener(new ConfigChangeListener());

		repositories.put(SystemRepository.ID, systemRepos);
	}

	public SystemRepository getSystemRepository() {
		return (SystemRepository)repositories.get(SystemRepository.ID);
	}

	/**
	 * Gets the repository that is known by the specified ID from this manager.
	 * 
	 * @param id
	 *        A repository ID.
	 * @return A Repository object, or <tt>null</tt> if no repository was known
	 *         for the specified ID.
	 * @throws RepositoryConfigException
	 *         If no repository could be created due to invalid or incomplete
	 *         configuration data.
	 */
	public Repository getRepository(String id)
		throws RepositoryConfigException, RepositoryException
	{
		synchronized (repositories) {
			Repository repository = repositories.get(id);

			if (repository == null) {
				// First call, create and initialize the repository.
				repository = createRepository(id);

				if (repository != null) {
					repositories.put(id, repository);
				}
			}

			return repository;
		}
	}

	/**
	 * Returns all inititalized repositories. This method returns fast as no lazy
	 * creation of repositories takes place.
	 * 
	 * @return An unmodifiable collection containing the initialized
	 *         repositories.
	 * @see #getAllRepositories()
	 */
	public Collection<Repository> getInitializedRepositories() {
		synchronized (repositories) {
			return new ArrayList<Repository>(repositories.values());
		}
	}

	/**
	 * Returns all configured repositories. This may be an expensive operation as
	 * it initializes repositories that have not been initialized yet.
	 * 
	 * @return The Set of all Repositories defined in the SystemRepository.
	 * @see #getInitializedRepositories()
	 */
	public Collection<Repository> getAllRepositories()
		throws RepositoryConfigException, RepositoryException
	{
		// Collect all repsitory IDs
		Set<String> idSet = new HashSet<String>();

		RepositoryConnection con = getSystemRepository().getConnection();
		try {
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SERQL, ALL_REPOSITORIES_QUERY);
			TupleQueryResult queryResult = tupleQuery.evaluate();

			try {
				while (queryResult.hasNext()) {
					BindingSet bindings = queryResult.next();
					Literal id = (Literal)bindings.getValue("ID");
					idSet.add(id.getLabel());
				}
			}
			finally {
				queryResult.close();
			}
		}
		catch (MalformedQueryException e) {
			logger.error("Preconfigured query was reported to be malformed", e);
			throw new RuntimeException(e);
		}
		catch (QueryEvaluationException e) {
			throw new RepositoryException(e);
		}
		finally {
			con.close();
		}

		// Get all repositories
		ArrayList<Repository> result = new ArrayList<Repository>(idSet.size());
		for (String id : idSet) {
			result.add(getRepository(id));
		}
		return result;
	}

	/**
	 * Creates and initializes the repository with the specified ID.
	 * 
	 * @param id
	 *        A repository ID.
	 * @return The created repository, or <tt>null</tt> if no such repository
	 *         exists.
	 * @throws RepositoryConfigException
	 *         If no repository could be created due to invalid or incomplete
	 *         configuration data.
	 */
	private Repository createRepository(String id)
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
		RepositoryFactory factory = repositoryRegistry.get(config.getType());
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
						"Delegate configured for repository that is not a DelegatingRepository: "
								+ delegate.getClass());
			}
		}

		return repository;
	}

	/**
	 * Shuts down all initialized user repositories.
	 * 
	 * @see #shutDown()
	 */
	public void refresh() {
		Iterator<Map.Entry<String, Repository>> iter = repositories.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Repository> entry = iter.next();
			String repositoryID = entry.getKey();
			Repository repository = entry.getValue();

			if (!SystemRepository.ID.equals(repositoryID)) {
				iter.remove();

				try {
					repository.shutDown();
				}
				catch (RepositoryException e) {
					logger.error("Failed to shut down repository", e);
				}
			}
		}
	}

	/**
	 * Shuts down all initialized repositories, including the SYSTEM repository.
	 * 
	 * @see #refresh()
	 */
	public void shutDown() {
		synchronized (repositories) {
			for (Repository repository : repositories.values()) {
				try {
					repository.shutDown();
				}
				catch (RepositoryException e) {
					logger.error("Repository shut down failed", e);
				}
			}

			repositories = new HashMap<String, Repository>();
		}
	}

	private class ConfigChangeListener extends RepositoryConnectionListenerAdapter {

		@Override
		public void commit(RepositoryConnection con)
		{
			refresh();
		}
	}
}
