/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager;

import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORY;
import static org.openrdf.repository.manager.SystemRepository.REPOSITORYID;

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

import info.aduna.io.file.FileUtil;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.DelegatingRepository;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.config.DelegatingRepositoryImplConfig;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryRegistry;
import org.openrdf.repository.event.base.RepositoryConnectionListenerAdapter;
import org.openrdf.repository.manager.config.LocalConfigManager;
import org.openrdf.repository.manager.config.SystemConfigManager;
import org.openrdf.repository.manager.templates.LocalTemplateManager;
import org.openrdf.store.StoreConfigException;
import org.openrdf.store.StoreException;

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

	private static final String TEMPLATES = "templates";

	private static final String CONFIGURATIONS = "configurations";

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

	/**
	 * Initializes the repository manager.
	 * 
	 * @throws StoreException
	 *         If the manager failed to initialize the SYSTEM repository.
	 */
	public void initialize()
		throws StoreConfigException
	{
		LocalTemplateManager templates = new LocalTemplateManager(new File(baseDir, TEMPLATES));
		templates.init();
		setConfigTemplateManager(templates);
		try {
			if (getRepositoryDir(SystemRepository.ID).isDirectory()) {
				// Sesame 2.0 directory
				File systemDir = getRepositoryDir(SystemRepository.ID);
				SystemRepository systemRepos = new SystemRepository(systemDir);
				systemRepos.initialize();
				
				systemRepos.addRepositoryConnectionListener(new ConfigChangeListener());
				Repository systemRepository = systemRepos;
				setRepositoryConfigManager(new SystemConfigManager(systemRepository));

				synchronized (initializedRepositories) {
					initializedRepositories.put(SystemRepository.ID, systemRepository);
				}
			} else {
				// Sesame 3.0 directory
				setRepositoryConfigManager(new LocalConfigManager(new File(baseDir, CONFIGURATIONS)));
			}
		}
		catch (StoreException e) {
			throw new StoreConfigException(e);
		}
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
	 * @see #getBaseDir
	 */
	public File resolvePath(String path) {
		return new File(getBaseDir(), path);
	}

	public File getRepositoryDir(String repositoryID) {
		File repositoriesDir = resolvePath(REPOSITORIES_DIR);
		return new File(repositoriesDir, repositoryID);
	}

	@Override
	protected Repository createRepository(String id)
		throws StoreConfigException, StoreException
	{
		Repository repository = null;

		RepositoryConfig repConfig = parse(getRepositoryConfig(id));
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
	 * @throws StoreConfigException
	 *         If no repository could be created due to invalid or incomplete
	 *         configuration data.
	 */
	private Repository createRepositoryStack(RepositoryImplConfig config)
		throws StoreConfigException
	{
		RepositoryFactory factory = RepositoryRegistry.getInstance().get(config.getType());
		if (factory == null) {
			throw new StoreConfigException("Unsupported repository type: " + config.getType());
		}

		Repository repository = factory.getRepository(config);

		if (config instanceof DelegatingRepositoryImplConfig) {
			RepositoryImplConfig delegateConfig = ((DelegatingRepositoryImplConfig)config).getDelegate();

			Repository delegate = createRepositoryStack(delegateConfig);

			try {
				((DelegatingRepository)repository).setDelegate(delegate);
			}
			catch (ClassCastException e) {
				throw new StoreConfigException(
						"Delegate specified for repository that is not a DelegatingRepository: "
								+ delegate.getClass());
			}
		}

		return repository;
	}

	@Override
	public String addRepositoryConfig(String id, Model config)
		throws StoreConfigException, StoreException
	{
		parse(config);
		return super.addRepositoryConfig(id, config);
	}

	private RepositoryConfig parse(Model config)
		throws StoreConfigException
	{
		Resource repositoryNode = config.filter(null, RDF.TYPE, REPOSITORY).subjects().iterator().next();
		RepositoryConfig repConfig = RepositoryConfig.create(config, repositoryNode);
		repConfig.validate();
		return repConfig;
	}

	@Override
	public RepositoryInfo getRepositoryInfo(String id)
		throws StoreConfigException
	{
		RepositoryConfig config = null;
		if (id.equals(SystemRepository.ID)) {
			config = new RepositoryConfig(id, new SystemRepositoryConfig());
		}
		else {
			config = parse(getRepositoryConfig(id));
		}

		RepositoryInfo repInfo = new RepositoryInfo();
		repInfo.setId(id);
		repInfo.setDescription(config.getTitle());
		try {
			repInfo.setLocation(getRepositoryDir(id).toURI().toURL());
		}
		catch (MalformedURLException mue) {
			throw new StoreConfigException("Location of repository does not resolve to a valid URL", mue);
		}

		repInfo.setReadable(true);
		repInfo.setWritable(true);

		return repInfo;
	}

	@Override
	public List<RepositoryInfo> getAllRepositoryInfos(boolean skipSystemRepo)
		throws StoreConfigException
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
			if (contexts != null && contexts.length == 0) {
				modifiedAllContextsByConnection.put(conn, true);
			}
			else {
				for (Resource context : OpenRDFUtil.notNull(contexts)) {
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
			// TODO The SYSTEM repository should be replaced
			try {
				List<String> ids = new ArrayList<String>();
				for (Resource ctx : con.getContextIDs().asList()) {
					Model model = new LinkedHashModel();
					con.match(null, null, null, false, ctx).addTo(model);
					String id = getConfigId(model);
					ids.add(id);
					if (hasRepositoryConfig(id)) {
						Model currently = getRepositoryConfig(id);
						if (!currently.equals(model)) {
							addRepositoryConfig(id, model);
						}
					} else {
						addRepositoryConfig(id, model);
					}
				}
				Set<String> old = new HashSet<String>(getRepositoryIDs());
				old.removeAll(ids);
				for (String id : old) {
					removeRepositoryConfig(id);
				}
			} catch (StoreException e) {
				throw new AssertionError(e);
			}
			catch (StoreConfigException e) {
				throw new AssertionError(e);
			}
		}

		private String getConfigId(Model config)
			throws StoreConfigException
		{
			Set<Value> ids = config.filter(null, REPOSITORYID, null).objects();
			if (ids.size() != 1)
				throw new StoreConfigException("Repository ID not found");
			return ids.iterator().next().stringValue();
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
