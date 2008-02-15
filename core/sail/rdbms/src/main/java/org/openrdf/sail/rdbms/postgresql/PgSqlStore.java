/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.postgresql;

import org.apache.commons.dbcp.BasicDataSource;
import org.openrdf.sail.SailException;
import org.openrdf.sail.rdbms.RdbmsStore;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;

/**
 * A convenient way to initialise a PostgreSQL RDF store.
 * 
 * @author James Leigh
 * 
 */
public class PgSqlStore extends RdbmsStore {
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
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			throw new RdbmsException(e);
		}
		StringBuilder url = new StringBuilder();
		url.append("jdbc:postgresql:");
		if (serverName != null) {
			url.append("//").append(serverName);
			if (portNumber > 0) {
				url.append(":").append(portNumber);
			}
			url.append("/");
		}
		url.append(databaseName);
		BasicDataSource ds = new BasicDataSource();
		ds.setUrl(url.toString());
		if (user != null) {
			ds.setUsername(user);
		} else {
			ds.setUsername(System.getProperty("user.name"));
		}
		if (password != null) {
			ds.setPassword(password);
		}
		PgSqlConnectionFactory factory = new PgSqlConnectionFactory();
		factory.setSail(this);
		factory.setDataSource(ds);
		setConnectionFactory(factory);
		super.initialize();
	}
}
