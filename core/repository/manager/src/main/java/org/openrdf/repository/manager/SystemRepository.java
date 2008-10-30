/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.config.RepositoryConfigUtil;
import org.openrdf.repository.event.base.NotifyingRepositoryWrapper;
import org.openrdf.store.StoreException;

/**
 * FIXME: do not extend NotifyingRepositoryWrapper, because SystemRepository
 * shouldn't expose RepositoryWrapper behaviour, just implement
 * NotifyingRepository.
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
 */
public class SystemRepository extends NotifyingRepositoryWrapper {

	/*-----------*
	 * Constants *
	 *-----------*/

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * The repository identifier for the system repository that contains the
	 * configuration data.
	 */
	public static final String ID = "SYSTEM";

	public static final String TITLE = "System configuration repository";

	public static final String REPOSITORY_TYPE = "openrdf:SystemRepository";

	/*-----------*
	 * Variables *
	 *-----------*/

	private boolean persist;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SystemRepository()
		throws StoreException
	{
		super();
		this.persist = false;
		try {
			Class<?> Sail = Class.forName("org.openrdf.sail.Sail");
			Class<?> MemoryStore = Class.forName("org.openrdf.sail.memory.MemoryStore");
			Class<?> SailRepository = Class.forName("org.openrdf.repository.sail.SailRepository");
			Object sail = MemoryStore.newInstance();
			Object repository = SailRepository.getConstructor(Sail).newInstance(sail);
			Repository repo = (Repository) repository;
			super.setDelegate(repo);
		} catch (Exception e) {
			throw new StoreException(e);
		}
	}

	public SystemRepository(File systemDir)
		throws StoreException
	{
		super();
		this.persist = true;
		try {
			Class<?> Sail = Class.forName("org.openrdf.sail.Sail");
			Class<?> MemoryStore = Class.forName("org.openrdf.sail.memory.MemoryStore");
			Class<?> SailRepository = Class.forName("org.openrdf.repository.sail.SailRepository");
			Object sail = MemoryStore.getConstructor(File.class).newInstance(systemDir);
			Object repository = SailRepository.getConstructor(Sail).newInstance(sail);
			Repository repo = (Repository) repository;
			super.setDelegate(repo);
		} catch (Exception e) {
			throw new StoreException(e);
		}
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public void initialize()
		throws StoreException
	{
		super.initialize();

		RepositoryConnection con = getConnection();
		try {
			if (con.isEmpty()) {
				logger.info("Initializing empty {} repository", ID);

				con.setAutoCommit(false);
				con.setNamespace("rdf", RDF.NAMESPACE);
				con.setNamespace("sys", RepositoryConfigSchema.NAMESPACE);

				if (persist) {
					RepositoryConfig repConfig = new RepositoryConfig(ID, TITLE, new SystemRepositoryConfig());
					RepositoryConfigUtil.updateRepositoryConfigs(con, repConfig);
				}

				con.commit();
			}
		}
		catch (RepositoryConfigException e) {
			throw new StoreException(e.getMessage(), e);
		}
		finally {
			con.close();
		}
	}

	@Override
	public void setDelegate(Repository delegate)
	{
		throw new UnsupportedOperationException("Setting delegate on system repository not allowed");
	}
}
