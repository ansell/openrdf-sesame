/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms;

import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Iterator;

import javax.imageio.spi.ServiceRegistry;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailMetaData;
import org.openrdf.sail.helpers.SailBase;
import org.openrdf.sail.helpers.SynchronizedSailConnection;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.store.StoreException;

/**
 * The RDBMS SAIL for relational database storage in Sesame. This class acts
 * both as a base class for database specific stores as well as a generic store
 * that can infer the type of database through the JDBC connection.
 * 
 * @author James Leigh
 */
public class RdbmsStore extends SailBase {

	private RdbmsConnectionFactory factory;

	private String jdbcDriver;

	private String url;

	private String user;

	private String password;

	private int maxTripleTables;

	private boolean triplesIndexed = true;

	private boolean sequenced = true;

	private BasicDataSource ds;

	public RdbmsStore() {
		super();
	}

	/**
	 * Creates a new RDBMS RDF Store using the provided database connection.
	 * 
	 * @param url
	 *        JDNI url of a DataSource
	 */
	public RdbmsStore(String url) {
		this.url = url;
	}

	/**
	 * Creates a new RDBMS RDF Store using the provided database connection.
	 * 
	 * @param url
	 *        JDNI url of a DataSource
	 * @param user
	 * @param password
	 */
	public RdbmsStore(String url, String user, String password) {
		this.url = url;
		this.user = user;
		this.password = password;
	}

	/**
	 * Creates a new RDBMS RDF Store using the provided database connection.
	 * 
	 * @param jdbcDriver
	 * @param jdbcUrl
	 */
	public RdbmsStore(String jdbcDriver, String jdbcUrl) {
		this.jdbcDriver = jdbcDriver;
		this.url = jdbcUrl;
	}

	/**
	 * Creates a new RDBMS RDF Store using the provided database connection.
	 * 
	 * @param jdbcDriver
	 * @param jdbcUrl
	 * @param user
	 * @param password
	 */
	public RdbmsStore(String jdbcDriver, String jdbcUrl, String user, String password) {
		this.jdbcDriver = jdbcDriver;
		this.url = jdbcUrl;
		this.user = user;
		this.password = password;
	}

	public SailMetaData getSailMetaData() {
		return new RdbmsStoreMetaData(this);
	}

	public int getMaxNumberOfTripleTables() {
		return maxTripleTables;
	}

	public void setMaxNumberOfTripleTables(int max) {
		maxTripleTables = max;
	}

	public boolean isIndexed() {
		return triplesIndexed;
	}

	public void setIndexed(boolean indexed)
		throws StoreException
	{
		triplesIndexed = indexed;
		if (factory != null) {
			factory.setTriplesIndexed(triplesIndexed);
		}
	}

	public boolean isSequenced() {
		return sequenced;
	}

	public void setSequenced(boolean useSequence) {
		this.sequenced = useSequence;
	}

	public void initialize()
		throws StoreException
	{
		if (factory == null) {
			try {
				factory = createFactory(jdbcDriver, url, user, password);
			}
			catch (StoreException e) {
				throw e;
			}
			catch (Exception e) {
				throw new RdbmsException(e);
			}
		}
		factory.setMaxNumberOfTripleTables(maxTripleTables);
		factory.setTriplesIndexed(triplesIndexed);
		factory.setSequenced(sequenced);
		factory.init();
	}

	public boolean isWritable()
		throws StoreException
	{
		return factory.isWritable();
	}

	public URL getLocation()
		throws StoreException
	{
		return factory.getLocation();
	}

	public RdbmsValueFactory getValueFactory() {
		return factory.getValueFactory();
	}

	@Override
	protected SailConnection getConnectionInternal()
		throws StoreException
	{
		SailConnection con = factory.createConnection();
		return new SynchronizedSailConnection(con);
	}

	@Override
	protected void shutDownInternal()
		throws StoreException
	{
		factory.shutDown();
		try {
			if (ds != null) {
				ds.close();
			}
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

	protected void setConnectionFactory(RdbmsConnectionFactory factory) {
		this.factory = factory;
	}

	protected void setBasicDataSource(BasicDataSource ds) {
		this.ds = ds;
	}

	private RdbmsConnectionFactory createFactory(String jdbcDriver, String url, String user, String password)
		throws Exception
	{
		if (jdbcDriver != null) {
			Class.forName(jdbcDriver);
		}
		DataSource ds = lookupDataSource(url, user, password);
		Connection con;
		if (user == null || url.startsWith("jdbc:")) {
			con = ds.getConnection();
		}
		else {
			con = ds.getConnection(user, password);
		}
		try {
			DatabaseMetaData metaData = con.getMetaData();
			RdbmsConnectionFactory factory = newFactory(metaData);
			factory.setSail(this);
			if (user == null || url.startsWith("jdbc:")) {
				factory.setDataSource(ds);
			}
			else {
				factory.setDataSource(ds, user, password);
			}
			return factory;
		}
		finally {
			con.close();
		}
	}

	private DataSource lookupDataSource(String url, String user, String password)
		throws NamingException
	{
		if (url.startsWith("jdbc:")) {
			BasicDataSource ds = new BasicDataSource();
			ds.setUrl(url);
			ds.setUsername(user);
			ds.setPassword(password);
			setBasicDataSource(ds);
			return ds;
		}
		return (DataSource)new InitialContext().lookup(url);
	}

	private RdbmsConnectionFactory newFactory(DatabaseMetaData metaData)
		throws SQLException
	{
		String dbn = metaData.getDatabaseProductName();
		String dbv = metaData.getDatabaseProductVersion();
		RdbmsConnectionFactory factory;
		Iterator<RdbmsProvider> providers;
		providers = ServiceRegistry.lookupProviders(RdbmsProvider.class);
		while (providers.hasNext()) {
			RdbmsProvider provider = providers.next();
			factory = provider.createRdbmsConnectionFactory(dbn, dbv);
			if (factory != null)
				return factory;
		}
		return new RdbmsConnectionFactory();
	}

}
