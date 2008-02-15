/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.postgresql;

import org.openrdf.sail.SailException;
import org.openrdf.sail.rdbms.RdbmsStore;
import org.postgresql.jdbc3.Jdbc3PoolingDataSource;

/**
 * A convenient way to initialise a PostgreSQL RDF store.
 * 
 * @author James Leigh
 * 
 */
public class PgSqlStore extends RdbmsStore {
	private Jdbc3PoolingDataSource source;
	private String name = genName();
	private String serverName;
	private String databaseName;
	private int portNumber;
	private String user;
	private String password;

	public PgSqlStore() {
		super();
	}

	public PgSqlStore(String databaseName) {
		setDatabaseName(databaseName);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public int getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public void initialize() throws SailException {
		source = new Jdbc3PoolingDataSource();
		source.setDataSourceName(name);
		if (serverName != null) {
			source.setServerName(serverName);
		}
		source.setDatabaseName(databaseName);
		if (portNumber > 0) {
			source.setPortNumber(portNumber);
		}
		if (user != null) {
			source.setUser(user);
		} else {
			source.setUser(System.getProperty("user.name"));
		}
		if (password != null) {
			source.setPassword(password);
		}
		PgSqlConnectionFactory factory = new PgSqlConnectionFactory();
		factory.setSail(this);
		factory.setDataSource(source);
		setConnectionFactory(factory);
		super.initialize();
	}

	@Override
	protected void shutDownInternal() throws SailException {
		super.shutDownInternal();
		source.close();
	}

	private String genName() {
		String hex = Integer.toHexString(System.identityHashCode(this));
		return getClass().getSimpleName() + "#" + hex;
	}
}
