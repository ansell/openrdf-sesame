/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager.config;

import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORY_CONTEXT;
import static org.openrdf.repository.manager.SystemRepository.REPOSITORYID;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.store.StoreConfigException;
import org.openrdf.store.StoreException;

public class SystemConfigManager implements RepositoryConfigManager {

	/*-----------*
	 * Constants *
	 *-----------*/

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Repository SYSTEM;

	private final ValueFactory vf;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new RepositoryConfigManager that operates on the specified
	 * system repository.
	 * 
	 * @param SYSTEM
	 *        The repository where configuration data for repositories can be
	 *        stored, among other things.
	 */
	public SystemConfigManager(Repository SYSTEM) {
		this.SYSTEM = SYSTEM;
		this.vf = SYSTEM.getValueFactory();
	}

	/*---------*
	 * Methods *
	 *---------*/

	public URL getLocation()
		throws MalformedURLException
	{
		return null;
	}

	/**
	 * Gets the SYSTEM repository.
	 */
	public Repository getSystemRepository() {
		return SYSTEM;
	}

	public Set<String> getIDs()
		throws StoreConfigException
	{
		Model model = getSystemModel();
		Set<String> ids = new HashSet<String>();
		for (Value obj : model.filter(null, REPOSITORYID, null).objects()) {
			ids.add(obj.stringValue());
		}
		return ids;
	}

	public Model getConfig(String repositoryID)
		throws StoreConfigException
	{
		Model model = getSystemModel();
		Literal id = vf.createLiteral(repositoryID);
		for (Statement idStatement : model.filter(null, REPOSITORYID, id)) {
			Resource context = idStatement.getContext();

			if (context == null) {
				throw new StoreConfigException("No configuration context for repository " + repositoryID);
			}

			return model.filter(null, null, null, context);
		}
		return null;
	}

	public void addConfig(String id, Model config)
		throws StoreConfigException
	{
		for (Value value : config.filter(null, REPOSITORYID, null).objects()) {
			removeConfig(value.stringValue());
		}
		
		Model model = new LinkedHashModel();
		Resource context = vf.createBNode(id);
		model.add(context, RDF.TYPE, REPOSITORY_CONTEXT);
		model.add(context, REPOSITORYID, new LiteralImpl(id), context);
		for (Statement st : config) {
			model.add(st.getSubject(), st.getPredicate(), st.getObject(), context);
		}

		addSystemModel(model);
	}

	public void updateConfig(String id, Model config)
		throws StoreConfigException
	{

		Model model = new LinkedHashModel();
		Resource context = vf.createBNode(id);
		model.add(context, RDF.TYPE, REPOSITORY_CONTEXT);
		model.add(context, REPOSITORYID, new LiteralImpl(id), context);
		model.filter(null, null, null, context).addAll(config);

		addSystemModel(model);
	}

	public boolean removeConfig(String repositoryID)
		throws StoreConfigException
	{
		logger.debug("Removing repository configuration for {}.", repositoryID);
		boolean isRemoved = false;

		Model model = getSystemModel();

		// clear existing context
		Literal id = vf.createLiteral(repositoryID);
		for (Resource ctx : model.filter(null, REPOSITORYID, id).contexts()) {
			clearSystemModel(ctx);
			isRemoved = true;
		}

		return isRemoved;
	}

	/**
	 * Gets the SYSTEM model.
	 */
	private Model getSystemModel()
		throws StoreConfigException
	{
		Repository system = getSystemRepository();
		try {
			RepositoryConnection con = system.getConnection();
			try {
				return con.match(null, null, null, false).addTo(new LinkedHashModel());
			}
			finally {
				con.close();
			}
		}
		catch (StoreException e) {
			throw new StoreConfigException(e);
		}
	}

	/**
	 * Save the SYSTEM model.
	 */
	private void addSystemModel(Model model)
		throws StoreConfigException
	{
		Repository systemRepo = getSystemRepository();
		try {
			RepositoryConnection con = systemRepo.getConnection();
			try {
				con.add(model);
			}
			finally {
				con.close();
			}
		}
		catch (StoreException e) {
			throw new StoreConfigException(e);
		}
	}

	/**
	 * Clear the SYSTEM model.
	 */
	private void clearSystemModel(Resource context)
		throws StoreConfigException
	{
		Repository systemRepo = getSystemRepository();
		try {
			RepositoryConnection con = systemRepo.getConnection();
			try {
				con.clear(context);
			}
			finally {
				con.close();
			}
		}
		catch (StoreException e) {
			throw new StoreConfigException(e);
		}
	}
}
