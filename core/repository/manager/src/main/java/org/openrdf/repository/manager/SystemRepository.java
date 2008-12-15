/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager;

import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORY_CONTEXT;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ModelImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.event.base.NotifyingRepositoryWrapper;
import org.openrdf.store.StoreConfigException;
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

	/** <tt>http://www.openrdf.org/config/repository#repositoryID</tt> */
	public final static URI REPOSITORYID = new URIImpl("http://www.openrdf.org/config/repository#repositoryID");
;

	/*-----------*
	 * Variables *
	 *-----------*/

	private boolean persist;

	/*--------------*
	 * Constructors *
	 *--------------*/

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
					RepositoryConfig repConfig = new RepositoryConfig(TITLE, new SystemRepositoryConfig());
					updateRepositoryConfigs(con, ID, repConfig);
				}

				con.commit();
			}
		}
		catch (StoreConfigException e) {
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

	/**
	 * Update the specified RepositoryConnection with the specified set of
	 * RepositoryConfigs. This will overwrite all existing configurations in the
	 * Repository that have a Repository ID occurring in these RepositoryConfigs.
	 * 
	 * Note: this method does NOT commit the updates on the connection.
	 * 
	 * @param con
	 *        the repository connection to perform the update on
	 * @param configs
	 *        The RepositoryConfigs that should be added to or updated in the
	 *        Repository. The RepositoryConfig's ID may already occur in the
	 *        Repository, in which case all previous configuration data for that
	 *        Repository will be cleared before the RepositoryConfig is added.
	 * 
	 * @throws StoreException
	 * @throws StoreConfigException
	 */
	private void updateRepositoryConfigs(RepositoryConnection con, String id, RepositoryConfig config)
		throws StoreException, StoreConfigException
	{
		ValueFactory vf = con.getValueFactory();

		boolean wasAutoCommit = con.isAutoCommit();
		con.setAutoCommit(false);

		Resource context = getContext(con, id);

		if (context != null) {
			con.clear(context);
		}
		else {
			context = vf.createBNode();
		}

		con.add(context, RDF.TYPE, REPOSITORY_CONTEXT);

		Model model = new ModelImpl();
		config.export(model);
		con.add(model, context);

		con.setAutoCommit(wasAutoCommit);
	}

	private Resource getContext(RepositoryConnection con, String repositoryID)
		throws StoreException, StoreConfigException
	{
		Resource context = null;

		Statement idStatement = getIDStatement(con, repositoryID);
		if (idStatement != null) {
			context = idStatement.getContext();
		}

		return context;
	}

	private Statement getIDStatement(RepositoryConnection con, String repositoryID)
		throws StoreException, StoreConfigException
	{
		Literal idLiteral = con.getValueFactory().createLiteral(repositoryID);
		List<Statement> idStatementList = con.match(null, REPOSITORYID, idLiteral, true).asList();

		if (idStatementList.size() == 1) {
			return idStatementList.get(0);
		}
		else if (idStatementList.isEmpty()) {
			return null;
		}
		else {
			throw new StoreConfigException("Multiple ID-statements for repository ID " + repositoryID);
		}
	}
}
