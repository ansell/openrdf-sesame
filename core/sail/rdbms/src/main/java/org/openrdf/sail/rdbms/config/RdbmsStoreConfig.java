/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.config;

import static java.text.MessageFormat.format;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.DATABASE;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.HOST;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.JDBC_DRIVER;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.MAX_TRIPLE_TABLES;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.PASSWORD;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.PORT;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.URL;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.URL_PROPERTIES;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.URL_TEMPLATE;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.USER;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.ModelException;
import org.openrdf.sail.config.SailImplConfigBase;
import org.openrdf.store.StoreConfigException;

/**
 * Holds the JDBC Driver, URL, user and password, as well as the database
 * layout.
 * 
 * @author James Leigh
 */
public class RdbmsStoreConfig extends SailImplConfigBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private String jdbcDriver;

	private String url;

	private String user;

	private String password;

	private int maxTripleTables = 256;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public RdbmsStoreConfig() {
		super(RdbmsStoreFactory.SAIL_TYPE);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public String getJdbcDriver() {
		return jdbcDriver;
	}

	public void setJdbcDriver(String jdbcDriver) {
		this.jdbcDriver = jdbcDriver;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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

	public int getMaxTripleTables() {
		return maxTripleTables;
	}

	public void setMaxTripleTables(int maxTripleTables) {
		this.maxTripleTables = maxTripleTables;
	}

	@Override
	public void validate()
		throws StoreConfigException
	{
		super.validate();

		if (url == null) {
			throw new StoreConfigException("No URL specified for RdbmsStore");
		}
	}

	@Override
	public Resource export(Model model) {
		Resource implNode = super.export(model);

		ValueFactory vf = ValueFactoryImpl.getInstance();

		if (jdbcDriver != null) {
			model.add(implNode, JDBC_DRIVER, vf.createLiteral(jdbcDriver));
		}
		if (url != null) {
			model.add(implNode, URL, vf.createLiteral(url));
		}
		if (user != null) {
			model.add(implNode, USER, vf.createLiteral(user));
		}
		if (password != null) {
			model.add(implNode, PASSWORD, vf.createLiteral(password));
		}
		model.add(implNode, MAX_TRIPLE_TABLES, vf.createLiteral(maxTripleTables));

		return implNode;
	}

	@Override
	public void parse(Model model, Resource implNode)
		throws StoreConfigException
	{
		super.parse(model, implNode);

		try {
			Literal jdbcDriverLit = model.filter(implNode, JDBC_DRIVER, null).objectLiteral();
			if (jdbcDriverLit != null) {
				setJdbcDriver(jdbcDriverLit.getLabel());
			}

			String template = model.filter(implNode, URL_TEMPLATE, null).objectString();
			String host = model.filter(implNode, HOST, null).objectString();
			String port = model.filter(implNode, PORT, null).objectString();
			String database = model.filter(implNode, DATABASE, null).objectString();
			String properties = model.filter(implNode, URL_PROPERTIES, null).objectString();
			if (template != null && database != null) {
				setUrl(format(template, host, port, database, properties));
			}

			Literal urlLit = model.filter(implNode, URL, null).objectLiteral();
			if (urlLit != null) {
				setUrl(urlLit.getLabel());
			}

			Literal userLit = model.filter(implNode, USER, null).objectLiteral();
			if (userLit != null) {
				setUser(userLit.getLabel());
			}

			Literal passwordLit = model.filter(implNode, PASSWORD, null).objectLiteral();
			if (passwordLit != null) {
				setPassword(passwordLit.getLabel());
			}

			Literal maxTripleTablesLit = model.filter(implNode, MAX_TRIPLE_TABLES, null).objectLiteral();
			if (maxTripleTablesLit != null) {
				try {
					setMaxTripleTables(maxTripleTablesLit.intValue());
				}
				catch (NumberFormatException e) {
					throw new StoreConfigException("Invalid value for maxTripleTables: " + maxTripleTablesLit);
				}
			}
		}
		catch (ModelException e) {
			throw new StoreConfigException(e.getMessage(), e);
		}
	}
}
