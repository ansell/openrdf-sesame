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
import org.openrdf.sail.rdbms.managers.HashManager;
import org.openrdf.sail.rdbms.managers.LiteralManager;
import org.openrdf.sail.rdbms.managers.NamespaceManager;
import org.openrdf.sail.rdbms.managers.PredicateManager;
import org.openrdf.sail.rdbms.managers.TransTableManager;
import org.openrdf.sail.rdbms.managers.TripleManager;
import org.openrdf.sail.rdbms.managers.TripleTableManager;
import org.openrdf.sail.rdbms.managers.UriManager;
import org.openrdf.sail.rdbms.optimizers.RdbmsQueryOptimizer;
import org.openrdf.sail.rdbms.optimizers.SelectQueryOptimizerFactory;
import org.openrdf.sail.rdbms.schema.BNodeTable;
import org.openrdf.sail.rdbms.schema.HashTable;
import org.openrdf.sail.rdbms.schema.IdSequence;
import org.openrdf.sail.rdbms.schema.IntegerIdSequence;
import org.openrdf.sail.rdbms.schema.LiteralTable;
import org.openrdf.sail.rdbms.schema.LongIdSequence;
import org.openrdf.sail.rdbms.schema.NamespacesTable;
import org.openrdf.sail.rdbms.schema.TableFactory;
import org.openrdf.sail.rdbms.schema.URITable;
import org.openrdf.sail.rdbms.schema.ValueTableFactory;

/**
 * Responsible to initialise and wire all components together that will be
 * needed to satisfy any sail connection request.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsConnectionFactory {
	private RdbmsStore sail;
	private DataSource ds;
	private String user;
	private String password;
	private Connection resourceInserts;
	private Connection literalInserts;
	private Connection hashLookups;
	private Connection nsAndTableIndexes;
	private NamespaceManager namespaces;
	private TripleTableManager tripleTableManager;
	private HashManager hashManager;
	private RdbmsValueFactory vf;
	private UriManager uriManager;
	private BNodeManager bnodeManager;
	private LiteralManager literalManager;
	private PredicateManager predicateManager;
	private int maxTripleTables;
	private boolean triplesIndexed = true;
	private boolean sequenced;
	private HashTable hashTable;
	private URITable uriTable;
	private BNodeTable bnodeTable;
	private LiteralTable literalTable;
	private IdSequence ids;

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

	public int getMaxNumberOfTripleTables() {
		return maxTripleTables;
	}

	public void setMaxNumberOfTripleTables(int max) {
		maxTripleTables = max;
	}

	public boolean isSequenced() {
		return sequenced || hashManager != null;
	}

	public void setSequenced(boolean useSequence) {
		this.sequenced = useSequence;
	}

	public boolean isTriplesIndexed() {
		return triplesIndexed;
	}

	public void setTriplesIndexed(boolean triplesIndexed)
		throws SailException
	{
		this.triplesIndexed = triplesIndexed;
		if (tripleTableManager != null) {
			try {
				if (triplesIndexed) {
					tripleTableManager.createTripleIndexes();
				}
				else {
					tripleTableManager.dropTripleIndexes();
				}
			}
			catch (SQLException e) {
				throw new RdbmsException(e);
			}
		}
	}

	public RdbmsValueFactory getValueFactory() {
		return vf;
	}

	public void init() throws SailException {
		try {
			nsAndTableIndexes = getConnection();
			resourceInserts = getConnection();
			literalInserts = getConnection();
			nsAndTableIndexes.setAutoCommit(true);
			resourceInserts.setAutoCommit(true);
			literalInserts.setAutoCommit(true);
			bnodeManager = new BNodeManager();
			uriManager = new UriManager();
			literalManager = new LiteralManager();
			ValueTableFactory tables = createValueTableFactory();
			tables.setSequenced(sequenced);
			if (sequenced) {
				ids = new IntegerIdSequence();
				tables.setIdSequence(ids);
				hashLookups = getConnection();
				hashLookups.setAutoCommit(true);
				hashManager = new HashManager();
				hashTable = tables.createHashTable(hashLookups, hashManager.getQueue());
				ids.setHashTable(hashTable);
				ids.init();
				hashManager.setHashTable(hashTable);
				hashManager.setBNodeManager(bnodeManager);
				hashManager.setLiteralManager(literalManager);
				hashManager.setUriManager(uriManager);
				hashManager.setIdSequence(ids);
				hashManager.init();
			} else {
				ids = new LongIdSequence();
				ids.init();
				tables.setIdSequence(ids);
			}
			namespaces = new NamespaceManager();
			namespaces.setConnection(resourceInserts);
			NamespacesTable nsTable = tables.createNamespacesTable(nsAndTableIndexes);
			nsTable.initialize();
			namespaces.setNamespacesTable(nsTable);
			namespaces.initialize();
			bnodeManager.setHashManager(hashManager);
			bnodeManager.setIdSequence(ids);
			uriManager.setHashManager(hashManager);
			uriManager.setIdSequence(ids);
			bnodeTable = tables.createBNodeTable(resourceInserts, bnodeManager.getQueue());
			uriTable = tables.createURITable(resourceInserts, uriManager.getQueue());
			literalManager.setHashManager(hashManager);
			literalManager.setIdSequence(ids);
			literalTable = tables.createLiteralTable(literalInserts, literalManager.getQueue());
			literalTable.setIdSequence(ids);
			vf = new RdbmsValueFactory();
			vf.setDelegate(ValueFactoryImpl.getInstance());
			vf.setIdSequence(ids);
			uriManager.setUriTable(uriTable);
			uriManager.init();
			predicateManager = new PredicateManager();
			predicateManager.setUriManager(uriManager);
			tripleTableManager = new TripleTableManager(tables);
			tripleTableManager.setConnection(nsAndTableIndexes);
			tripleTableManager.setIdSequence(ids);
			tripleTableManager.setBNodeManager(bnodeManager);
			tripleTableManager.setUriManager(uriManager);
			tripleTableManager.setLiteralManager(literalManager);
			tripleTableManager.setHashManager(hashManager);
			tripleTableManager.setPredicateManager(predicateManager);
			tripleTableManager.setMaxNumberOfTripleTables(maxTripleTables);
			tripleTableManager.setIndexingTriples(triplesIndexed);
			tripleTableManager.initialize();
			if (triplesIndexed) {
				tripleTableManager.createTripleIndexes();
			}
			else {
				tripleTableManager.dropTripleIndexes();
			}
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
			return !nsAndTableIndexes.isReadOnly();
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
			s.setLiteralTable(literalTable);
			s.setIdSequence(ids);
			DefaultSailChangedEvent sailChangedEvent = new DefaultSailChangedEvent(sail);
			s.setSailChangedEvent(sailChangedEvent);
			TableFactory tables = createTableFactory();
			TransTableManager trans = createTransTableManager();
			trans.setIdSequence(ids);
			tripleManager.setTransTableManager(trans);
			trans.setBatchQueue(tripleManager.getQueue());
			trans.setSailChangedEvent(sailChangedEvent);
			trans.setConnection(db);
			trans.setTemporaryTableFactory(tables);
			trans.setStatementsTable(tripleTableManager);
			trans.setFromDummyTable(getFromDummyTable());
			trans.initialize();
			s.setTransaction(trans);
			QueryBuilderFactory bfactory = createQueryBuilderFactory();
			bfactory.setValueFactory(vf);
			bfactory.setUsingHashTable(hashManager != null);
			s.setQueryBuilderFactory(bfactory);
			RdbmsConnection conn = new RdbmsConnection(sail, s);
			conn.setNamespaces(namespaces);
			RdbmsEvaluationFactory efactory = new RdbmsEvaluationFactory();
			efactory.setQueryBuilderFactory(bfactory);
			efactory.setRdbmsTripleRepository(s);
			efactory.setIdSequence(ids);
			conn.setRdbmsEvaluationFactory(efactory);
			RdbmsQueryOptimizer optimizer = createOptimizer();
			SelectQueryOptimizerFactory selectOptimizerFactory = createSelectQueryOptimizerFactory();
			selectOptimizerFactory.setTransTableManager(trans);
			selectOptimizerFactory.setValueFactory(vf);
			selectOptimizerFactory.setIdSequence(ids);
			optimizer.setSelectQueryOptimizerFactory(selectOptimizerFactory);
			optimizer.setValueFactory(vf);
			optimizer.setBnodeTable(bnodeTable);
			optimizer.setUriTable(uriTable);
			optimizer.setLiteralTable(literalTable);
			optimizer.setHashTable(hashTable);
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
			if (hashManager != null) {
				hashManager.close();
			}
			if (resourceInserts != null) {
				resourceInserts.close();
				resourceInserts = null;
			}
			if (literalInserts != null) {
				literalInserts.close();
				literalInserts = null;
			}
			if (hashLookups != null) {
				hashLookups.close();
				hashLookups = null;
			}
			if (nsAndTableIndexes != null) {
				nsAndTableIndexes.close();
				nsAndTableIndexes = null;
			}
		} catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

	protected QueryBuilderFactory createQueryBuilderFactory() {
		return new QueryBuilderFactory();
	}

	protected ValueTableFactory createValueTableFactory() {
		return new ValueTableFactory(createTableFactory());
	}

	protected TableFactory createTableFactory() {
		return new TableFactory();
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
