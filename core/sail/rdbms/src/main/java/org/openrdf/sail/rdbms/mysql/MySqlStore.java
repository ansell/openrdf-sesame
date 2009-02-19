/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.mysql;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.dbcp.BasicDataSource;

import org.openrdf.sail.rdbms.RdbmsStore;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.store.StoreException;

/**
 * A convenient way to initialise a MySql RDF store.
 * 
 * @author James Leigh
 * 
 */
public class MySqlStore extends RdbmsStore {

	private String serverName = "localhost";

	private String databaseName;

	private int portNumber;

	private Map<String, String> properties = Collections.emptyMap();

	private String user;

	private String password;

	public MySqlStore() {
		super();
	}

	public MySqlStore(String databaseName) {
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

	public Map<String, String> getProperties() {
		return Collections.unmodifiableMap(properties);
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = new HashMap<String, String>(properties);
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
	public void initialize()
		throws StoreException
	{
		try {
			Class.forName("com.mysql.jdbc.Driver");
		}
		catch (ClassNotFoundException e) {
			throw new RdbmsException(e.toString(), e);
		}
		StringBuilder url = new StringBuilder();
		url.append("jdbc:mysql:");
		if (serverName != null) {
			url.append("//").append(serverName);
			if (portNumber > 0) {
				url.append(":").append(portNumber);
			}
			url.append("/");
		}
		url.append(databaseName);
		url.append("?useUnicode=yes&characterEncoding=UTF-8");
		for (Entry<String, String> e : getProperties().entrySet()) {
			url.append("&");
			url.append(enc(e.getKey()));
			url.append("=");
			url.append(enc(e.getValue()));
		}
		BasicDataSource ds = new BasicDataSource();
		ds.setUrl(url.toString());
		if (user != null) {
			ds.setUsername(user);
		}
		else {
			ds.setUsername(System.getProperty("user.name"));
		}
		if (password != null) {
			ds.setPassword(password);
		}
		MySqlConnectionFactory factory = new MySqlConnectionFactory();
		factory.setSail(this);
		factory.setDataSource(ds);
		setBasicDataSource(ds);
		setConnectionFactory(factory);
		super.initialize();
	}

	private String enc(String text) {
		try {
			return URLEncoder.encode(text, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}
}
