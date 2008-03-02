/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.rdbms.evaluation.QueryBuilderFactory;
import org.openrdf.sail.rdbms.evaluation.RdbmsEvaluationFactory;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.managers.BNodeManager;
import org.openrdf.sail.rdbms.managers.LiteralManager;
import org.openrdf.sail.rdbms.managers.NamespaceManager;
import org.openrdf.sail.rdbms.managers.PredicateManager;
import org.openrdf.sail.rdbms.managers.UriManager;
import org.openrdf.sail.rdbms.optimizers.RdbmsQueryOptimizer;
import org.openrdf.sail.rdbms.optimizers.SelectQueryOptimizerFactory;
import org.openrdf.sail.rdbms.schema.LiteralTable;
import org.openrdf.sail.rdbms.schema.NamespacesTable;
import org.openrdf.sail.rdbms.schema.TripleTableManager;
import org.openrdf.sail.rdbms.schema.RdbmsTableFactory;
import org.openrdf.sail.rdbms.schema.ResourceTable;
import org.openrdf.sail.rdbms.schema.TransTableManager;

/**
 * Responsible to initialise and wire all components together that will be
 * needed to satisfy any sail connection request.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsConnectionFactory {
	private RdbmsStore sail;
	private ResourceTable bnodeTable;
	private DataSource ds;
	private String user;
	private String password;
	private Connection lookup;
	private Connection literalLookup;
	private Connection index;
	private LiteralTable literalTable;
	private NamespaceManager namespaces;
	private TripleTableManager predicateTableManager;
	private ResourceTable uriTable;
	private ResourceTable longUriTable;
	private RdbmsValueFactory vf;
	private UriManager uriManager;
	private BNodeManager bnodeManager;
	private LiteralManager literalManager;
	private PredicateManager predicateManager;

	public void setSail(RdbmsStore sail) {
		this.sail = sail;
	}

	public DataSource getDataSource() {
		return ds;
	}

	public void setDataSource(DataSource ds) {
		this.ds = ds;
	}

	public void setDataSource(DataSource ds, String user, String password) {
		this.ds = ds;
		this.user = user;
		this.password = password;
	}

	public RdbmsValueFactory getValueFactory() {
		return vf;
	}

	public void init() throws SailException {
		try {
			index = getConnection();
			lookup = getConnection();
			literalLookup = getConnection();
			index.setAutoCommit(true);
			lookup.setAutoCommit(true);
			literalLookup.setAutoCommit(true);
			RdbmsTableFactory tables = createRdbmsTableFactory();
			namespaces = new NamespaceManager();
			namespaces.setConnection(lookup);
			NamespacesTable nsTable = tables.createNamespacesTable(index);
			nsTable.initialize();
			namespaces.setNamespacesTable(nsTable);
			namespaces.initialize();
			bnodeTable = tables.createBNodeTable(lookup);
			bnodeTable.initialize();
			uriTable = tables.createURITable(lookup);
			uriTable.initialize();
			longUriTable = tables.createLongURITable(lookup);
			longUriTable.initialize();
			literalTable = tables.createLiteralTable(literalLookup);
			literalTable.initialize();
			vf = new RdbmsValueFactory();
			vf.setDelegate(ValueFactoryImpl.getInstance());
			uriManager = new UriManager(uriTable, longUriTable);
			uriManager.init();
			predicateManager = new PredicateManager();
			predicateManager.setUriManager(uriManager);
			predicateTableManager = new TripleTableManager(tables);
			predicateTableManager.setConnection(index);
			predicateTableManager.setBNodeTable(bnodeTable);
			predicateTableManager.setURITable(uriTable);
			predicateTableManager.setLongUriTable(longUriTable);
			predicateTableManager.setLiteralTable(literalTable);
			predicateTableManager.setPredicateManager(predicateManager);
			predicateTableManager.initialize();
			bnodeManager = new BNodeManager(bnodeTable);
			bnodeManager.init();
			vf.setBNodeManager(bnodeManager);
			vf.setURIManager(uriManager);
			literalManager = new LiteralManager(literalTable);
			literalManager.init();
			vf.setLiteralManager(literalManager);
			vf.setPredicateManager(predicateManager);
		} catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

	public boolean isWritable() throws SailException {
		try {
			return !index.isReadOnly();
		} catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

	public SailConnection createConnection() throws SailException {
		try {
			Connection db = getConnection();
			db.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			db.setAutoCommit(true);
			RdbmsTripleRepository s = new RdbmsTripleRepository();
			s.setValueFactory(vf);
			s.setConnection(db);
			s.setBNodeTable(bnodeTable);
			s.setURITable(uriTable);
			s.setLongUriTable(longUriTable);
			s.setLiteralTable(literalTable);
			RdbmsTableFactory tables = createRdbmsTableFactory();
			TransTableManager trans = createTransTableManager();
			trans.setConnection(db);
			trans.setRdbmsTableFactory(tables);
			trans.setStatementsTable(predicateTableManager);
			trans.setFromDummyTable(getFromDummyTable());
			trans.initialize();
			s.setTransaction(trans);
			QueryBuilderFactory bfactory = createQueryBuilderFactory();
			bfactory.setValueFactory(vf);
			s.setQueryBuilderFactory(bfactory);
			RdbmsConnection conn = new RdbmsConnection(sail, s);
			conn.setNamespaces(namespaces);
			RdbmsEvaluationFactory efactory = new RdbmsEvaluationFactory();
			efactory.setQueryBuilderFactory(bfactory);
			efactory.setRdbmsTripleRepository(s);
			conn.setRdbmsEvaluationFactory(efactory);
			RdbmsQueryOptimizer optimizer = createOptimizer();
			SelectQueryOptimizerFactory selectOptimizerFactory = createSelectQueryOptimizerFactory();
			selectOptimizerFactory.setTransTableManager(trans);
			selectOptimizerFactory.setValueFactory(vf);
			optimizer.setSelectQueryOptimizerFactory(selectOptimizerFactory);
			optimizer.setValueFactory(vf);
			optimizer.setBnodeTable(bnodeTable);
			optimizer.setUriTable(uriTable);
			optimizer.setLongUriTable(longUriTable);
			optimizer.setLiteralTable(literalTable);
			conn.setRdbmsQueryOptimizer(optimizer);
			return conn;
		} catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

	public void shutDown() throws SailException {
		try {
			if (predicateTableManager != null) {
				predicateTableManager.close();
			}
			if (uriManager != null) {
				uriManager.close();
			}
			if (bnodeManager != null) {
				bnodeManager.close();
			}
			if (literalManager != null) {
				literalManager.close();
			}
			if (lookup != null) {
				lookup.close();
				lookup = null;
			}
			if (literalLookup != null) {
				literalLookup.close();
				literalLookup = null;
			}
			if (index != null) {
				index.close();
				index = null;
			}
		} catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

	protected QueryBuilderFactory createQueryBuilderFactory() {
		return new QueryBuilderFactory();
	}

	protected RdbmsTableFactory createRdbmsTableFactory() {
		return new RdbmsTableFactory();
	}

	protected TransTableManager createTransTableManager() {
		return new TransTableManager();
	}

	protected RdbmsQueryOptimizer createOptimizer() {
		return new RdbmsQueryOptimizer();
	}

	protected SelectQueryOptimizerFactory createSelectQueryOptimizerFactory() {
		return new SelectQueryOptimizerFactory();
	}

	protected String getFromDummyTable() {
		return "FROM DUAL";
	}

	protected Connection getConnection() throws SQLException {
		if (user == null)
			return ds.getConnection();
		return ds.getConnection(user, password);
	}

}
