/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager.config;

import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORYID;
import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORY_CONTEXT;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.StoreException;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ModelImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.sail.LockManager;
import org.openrdf.sail.SailReadOnlyException;
import org.openrdf.sail.helpers.DirectoryLockManager;

public class SystemConfigManager implements RepositoryConfigManager {

	/*-----------*
	 * Constants *
	 *-----------*/

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	/*-----------*
	 * Variables *
	 *-----------*/

	private final Repository SYSTEM;

	private final ValueFactory vf;

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
	public SystemConfigManager(Repository SYSTEM) {
		this.SYSTEM = SYSTEM;
		this.vf = SYSTEM.getValueFactory();
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the URL of the server or directory.
	 * 
	 * @throws MalformedURLException If the location cannot be represented as a URL.
	 */
	public URL getLocation() throws MalformedURLException {
		return null;
	}

	/**
	 * Gets the SYSTEM repository.
	 */
	public Repository getSystemRepository() {
		return SYSTEM;
	}

	public Set<String> getIDs()
		throws RepositoryConfigException
	{
		Model model = getSystemModel();
		Set<String> ids = new HashSet<String>();
		for (Value obj : model.objects(null, REPOSITORYID)) {
			ids.add(obj.stringValue());
		}
		return ids;
	}

	public Model getConfig(String repositoryID)
		throws RepositoryConfigException
	{
		Model model = getSystemModel();
		Literal id = vf.createLiteral(repositoryID);
		for (Statement idStatement : model.filter(null, REPOSITORYID, id)) {
			Resource context = idStatement.getContext();

			if (context == null) {
				throw new RepositoryConfigException("No configuration context for repository " + repositoryID);
			}

			return model.filter(null, null, null, context);
		}
		return null;
	}

	public void addConfig(Model config)
		throws RepositoryConfigException
	{
		Model model = new ModelImpl();
		Resource context = vf.createBNode();
		model.add(context, RDF.TYPE, REPOSITORY_CONTEXT);
		for (Statement st : config) {
			model.add(st.getSubject(), st.getPredicate(), st.getObject(), context);
		}

		addSystemModel(model);
	}

	public void updateConfig(Model config)
		throws RepositoryConfigException
	{
		for (Value value : config.objects(null, REPOSITORYID)) {
			removeConfig(value.stringValue());
		}

		Model model = new ModelImpl();
		Resource context = vf.createBNode();
		model.add(context, RDF.TYPE, REPOSITORY_CONTEXT);
		model.filter(null, null, null, context).addAll(config);

		addSystemModel(model);
	}

	public void removeConfig(String repositoryID)
		throws RepositoryConfigException
	{
		logger.info("Removing repository configuration for {}.", repositoryID);
		boolean isRemoved = false;

			Model model = getSystemModel();

			// clear existing context
			Literal id = vf.createLiteral(repositoryID);
			for (Resource ctx : model.contexts(null, REPOSITORYID, id)) {
				clearSystemModel(ctx);
				isRemoved = true;
			}

		if (!isRemoved)
			throw new RepositoryConfigException("No such repository config");
	}

	/**
	 * Gets the SYSTEM model.
	 */
	private Model getSystemModel()
		throws RepositoryConfigException
	{
		Repository system = getSystemRepository();
		try {
			RepositoryConnection con = system.getConnection();
			try {
				return con.getStatements(null, null, null, false).addTo(new ModelImpl());
			}
			finally {
				con.close();
			}
		}
		catch (StoreException e) {
			throw new RepositoryConfigException(e);
		}
	}

	/**
	 * Save the SYSTEM model.
	 */
	private void addSystemModel(Model model)
		throws RepositoryConfigException
	{
		Repository systemRepo = getSystemRepository();
		try {
			try {
				RepositoryConnection con = systemRepo.getConnection();
				try {
					con.add(model);
				}
				finally {
					con.close();
				}
			}
			catch (SailReadOnlyException e) {
				if (tryToRemoveLock(e, systemRepo)) {
					addSystemModel(model);
				} else {
					throw e;
				}
			}
		}
		catch (IOException e) {
			throw new RepositoryConfigException(e);
		}
		catch (StoreException e) {
			throw new RepositoryConfigException(e);
		}
	}

	/**
	 * Clear the SYSTEM model.
	 */
	private void clearSystemModel(Resource context)
		throws RepositoryConfigException
	{
		Repository systemRepo = getSystemRepository();
		try {
			try {
				RepositoryConnection con = systemRepo.getConnection();
				try {
					con.clear(context);
				}
				finally {
					con.close();
				}
			}
			catch (SailReadOnlyException e) {
				if (tryToRemoveLock(e, systemRepo)) {
					clearSystemModel(context);
				} else {
					throw e;
				}
			}
		}
		catch (IOException e) {
			throw new RepositoryConfigException(e);
		}
		catch (StoreException e) {
			throw new RepositoryConfigException(e);
		}
	}

	private boolean tryToRemoveLock(SailReadOnlyException e, Repository repo)
		throws IOException, StoreException
	{
		boolean lockRemoved = false;

		LockManager lockManager = new DirectoryLockManager(repo.getDataDir());

		if (lockManager.isLocked()) {
			repo.shutDown();
			lockRemoved = lockManager.revokeLock();
			repo.initialize();
		}

		return lockRemoved;
	}
}
