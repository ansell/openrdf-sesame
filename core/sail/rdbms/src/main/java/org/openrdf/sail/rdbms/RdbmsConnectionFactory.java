/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms;

import static java.sql.Connection.TRANSACTION_READ_COMMITTED;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.DefaultSailChangedEvent;
import org.openrdf.sail.rdbms.evaluation.QueryBuilderFactory;
import org.openrdf.sail.rdbms.evaluation.RdbmsEvaluationFactory;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.managers.BNodeManager;
import org.openrdf.sail.rdbms.managers.LiteralManager;
import org.openrdf.sail.rdbms.managers.NamespaceManager;
import org.openrdf.sail.rdbms.managers.PredicateManager;
import org.openrdf.sail.rdbms.managers.TripleManager;
import org.openrdf.sail.rdbms.managers.UriManager;
import org.openrdf.sail.rdbms.optimizers.RdbmsQueryOptimizer;
import org.openrdf.sail.rdbms.optimizers.SelectQueryOptimizerFactory;
import org.openrdf.sail.rdbms.schema.LiteralTable;
import org.openrdf.sail.rdbms.schema.NamespacesTable;
import org.openrdf.sail.rdbms.schema.RdbmsTableFactory;
import org.openrdf.sail.rdbms.schema.ResourceTable;
import org.openrdf.sail.rdbms.schema.TransTableManager;
import org.openrdf.sail.rdbms.schema.TripleTableManager;

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
	private TripleTableManager tripleTableManager;
	private ResourceTable uriTable;
	private ResourceTable longUriTable;
	private RdbmsValueFactory vf;
	private UriManager uriManager;
	private BNodeManager bnodeManager;
	private LiteralManager literalManager;
	private PredicateManager predicateManager;
	private int maxTripleTables;

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

	public void setMaxNumberOfTripleTables(int max) {
		maxTripleTables = max;
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
			bnodeManager = new BNodeManager();
			uriManager = new UriManager();
			bnodeTable = tables.createBNodeTable(lookup, bnodeManager.getQueue());
			bnodeTable.initialize();
			uriTable = tables.createURITable(lookup, uriManager.getQueue());
			uriTable.initialize();
			longUriTable = tables.createLongURITable(lookup, uriManager.getQueue());
			longUriTable.initialize();
			literalManager = new LiteralManager();
			literalTable = tables.createLiteralTable(literalLookup, literalManager.getQueue());
			literalTable.initialize();
			vf = new RdbmsValueFactory();
			vf.setDelegate(ValueFactoryImpl.getInstance());
			uriManager.setLonger(longUriTable);
			uriManager.setShorter(uriTable);
			uriManager.init();
			predicateManager = new PredicateManager();
			predicateManager.setUriManager(uriManager);
			tripleTableManager = new TripleTableManager(tables);
			tripleTableManager.setConnection(index);
			tripleTableManager.setBNodeTable(bnodeTable);
			tripleTableManager.setURITable(uriTable);
			tripleTableManager.setLongUriTable(longUriTable);
			tripleTableManager.setLiteralTable(literalTable);
			tripleTableManager.setPredicateManager(predicateManager);
			tripleTableManager.setMaxNumberOfTripleTables(maxTripleTables);
			tripleTableManager.initialize();
			bnodeManager.setTable(bnodeTable);
			bnodeManager.init();
			vf.setBNodeManager(bnodeManager);
			vf.setURIManager(uriManager);
			literalManager.setTable(literalTable);
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
			db.setAutoCommit(true);
			if (db.getTransactionIsolation() != TRANSACTION_READ_COMMITTED) {
				db.setTransactionIsolation(TRANSACTION_READ_COMMITTED);
			}
			TripleManager tripleManager = new TripleManager();
			RdbmsTripleRepository s = new RdbmsTripleRepository();
			s.setTripleManager(tripleManager);
			s.setValueFactory(vf);
			s.setConnection(db);
			s.setBNodeTable(bnodeTable);
			s.setURITable(uriTable);
			s.setLongUriTable(longUriTable);
			s.setLiteralTable(literalTable);
			DefaultSailChangedEvent sailChangedEvent = new DefaultSailChangedEvent(sail);
			s.setSailChangedEvent(sailChangedEvent);
			RdbmsTableFactory tables = createRdbmsTableFactory();
			TransTableManager trans = createTransTableManager();
			tripleManager.setTransTableManager(trans);
			trans.setBatchQueue(tripleManager.getQueue());
			trans.setSailChangedEvent(sailChangedEvent);
			trans.setConnection(db);
			trans.setRdbmsTableFactory(tables);
			trans.setStatementsTable(tripleTableManager);
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
			if (tripleTableManager != null) {
				tripleTableManager.close();
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
