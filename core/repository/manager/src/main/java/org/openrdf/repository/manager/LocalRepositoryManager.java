/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager;

import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORYID;
import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORY_CONTEXT;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import info.aduna.io.FileUtil;

import org.openrdf.StoreException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.DelegatingRepository;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.config.DelegatingRepositoryImplConfig;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryConfigSchema;
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
		throws StoreException
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
	 * Gets the base dir against which to resolve relative paths.
	 * 
	 * @throws MalformedURLException If the path cannot be parsed as a URL
	 */
	public URL getLocation() throws MalformedURLException {
		return baseDir.toURI().toURL();
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
		throws RepositoryConfigException, StoreException
	{
		Repository repository = null;

		RepositoryConfig repConfig = getRepositoryConfig(id);
		if (repConfig != null) {
			repConfig.validate();

			repository = createRepositoryStack(repConfig.getRepositoryImplConfig());
			repository.setDataDir(getRepositoryDir(id));
			repository.initialize();
		}

		return repository;
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
	public RepositoryInfo getRepositoryInfo(String id)
		throws RepositoryConfigException
	{
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
			throw new RepositoryConfigException("Location of repository does not resolve to a valid URL", mue);
		}

		repInfo.setReadable(true);
		repInfo.setWritable(true);

		return repInfo;
	}

	@Override
	public List<RepositoryInfo> getAllRepositoryInfos(boolean skipSystemRepo)
		throws RepositoryConfigException
	{
		List<RepositoryInfo> result = new ArrayList<RepositoryInfo>();

		for (String id : getRepositoryIDs()) {
			if (!skipSystemRepo || !id.equals(SystemRepository.ID)) {
				result.add(getRepositoryInfo(id));
			}
		}

		return result;
	}

	class ConfigChangeListener extends RepositoryConnectionListenerAdapter {

		private Map<RepositoryConnection, Set<Resource>> modifiedContextsByConnection = new HashMap<RepositoryConnection, Set<Resource>>();

		private Map<RepositoryConnection, Boolean> modifiedAllContextsByConnection = new HashMap<RepositoryConnection, Boolean>();

		private Map<RepositoryConnection, Set<Resource>> removedContextsByConnection = new HashMap<RepositoryConnection, Set<Resource>>();

		private Set<Resource> getModifiedContexts(RepositoryConnection conn) {
			Set<Resource> result = modifiedContextsByConnection.get(conn);
			if (result == null) {
				result = new HashSet<Resource>();
				modifiedContextsByConnection.put(conn, result);
			}
			return result;
		}

		private Set<Resource> getRemovedContexts(RepositoryConnection conn) {
			Set<Resource> result = removedContextsByConnection.get(conn);
			if (result == null) {
				result = new HashSet<Resource>();
				removedContextsByConnection.put(conn, result);
			}
			return result;
		}

		private void registerModifiedContexts(RepositoryConnection conn, Resource... contexts) {
			Set<Resource> modifiedContexts = getModifiedContexts(conn);
			// wildcard used for context
			if (contexts == null) {
				modifiedAllContextsByConnection.put(conn, true);
			}
			else {
				for (Resource context : contexts) {
					modifiedContexts.add(context);
				}
			}
		}

		@Override
		public void add(RepositoryConnection conn, Resource subject, URI predicate, Value object,
				Resource... contexts)
		{
			registerModifiedContexts(conn, contexts);
		}

		@Override
		public void clear(RepositoryConnection conn, Resource... contexts) {
			registerModifiedContexts(conn, contexts);
		}

		@Override
		public void remove(RepositoryConnection conn, Resource subject, URI predicate, Value object,
				Resource... contexts)
		{
			if (object != null && object.equals(RepositoryConfigSchema.REPOSITORY_CONTEXT)) {
				if (subject == null) {
					modifiedAllContextsByConnection.put(conn, true);
				}
				else {
					Set<Resource> removedContexts = getRemovedContexts(conn);
					removedContexts.add(subject);
				}
			}
			registerModifiedContexts(conn, contexts);
		}

		@Override
		public void rollback(RepositoryConnection conn) {
			modifiedContextsByConnection.remove(conn);
			modifiedAllContextsByConnection.remove(conn);
		}

		@Override
		public void commit(RepositoryConnection con) {
			// refresh all contexts when a wildcard was used
			// REMIND: this could still be improved if we knew whether or not a
			// *repositoryconfig* context was actually modified
			Boolean fullRefreshNeeded = modifiedAllContextsByConnection.remove(con);
			if (fullRefreshNeeded != null && fullRefreshNeeded.booleanValue()) {
				logger.debug("Reacting to commit on SystemRepository for all contexts");
				refresh();
			}
			// refresh only modified contexts that actually contain repository
			// configurations
			else {
				Set<Resource> modifiedContexts = modifiedContextsByConnection.remove(con);
				Set<Resource> removedContexts = removedContextsByConnection.remove(con);
				if(removedContexts != null && !removedContexts.isEmpty()) {
					modifiedContexts.removeAll(removedContexts);
				}
				if (modifiedContexts != null) {
					logger.debug("React to commit on SystemRepository for contexts {}", modifiedContexts);
					// refresh all modified contexts
					for (Resource context : modifiedContexts) {
						logger.debug("Processing modified context {}.", context);
						try {
							if (isRepositoryConfigContext(con, context)) {
								String repositoryID = getRepositoryID(con, context);
								logger.debug("Reacting to modified repository config for {}", repositoryID);
								Repository repository = removeInitializedRepository(repositoryID);
								if (repository != null) {
									logger.debug("Modified repository {} has been initialized, refreshing...", repositoryID);
									// refresh single repository
									refreshRepository(repositoryID, repository);
								}
								else {
									logger.debug("Modified repository {} has not been initialized, skipping...", repositoryID);
								}
							}
							else {
								logger.debug("Context {} doesn't contain repository config information.", context);
							}
						}
						catch (StoreException re) {
							logger.error("Failed to process repository configuration changes", re);
						}
					}
				}
			}
		}

		private boolean isRepositoryConfigContext(RepositoryConnection con, Resource context)
			throws StoreException
		{
			logger.debug("Is {} a repository config context?", context);
			return con.hasStatement(context, RDF.TYPE, REPOSITORY_CONTEXT, true, (Resource)null);
		}

		private String getRepositoryID(RepositoryConnection con, Resource context)
			throws StoreException
		{
			String result = null;

			RepositoryResult<Statement> idStatements = con.getStatements(null, REPOSITORYID, null, true, context);
			if (idStatements.hasNext()) {
				Statement idStatement = idStatements.next();
				result = idStatement.getObject().stringValue();
			}

			return result;
		}
	}

	@Override
	protected void cleanUpRepository(String repositoryID)
		throws IOException
	{
		File dataDir = getRepositoryDir(repositoryID);

		if (dataDir.isDirectory()) {
			logger.debug("Cleaning up data dir {} for repository {}", dataDir.getAbsolutePath(), repositoryID);
			FileUtil.deleteDir(dataDir);
		}
	}
}
