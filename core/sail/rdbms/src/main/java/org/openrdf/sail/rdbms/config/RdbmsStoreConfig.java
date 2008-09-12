/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.config;

import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.JDBC_DRIVER;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.MAX_TRIPLE_TABLES;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.PASSWORD;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.URL;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.USER;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailImplConfigBase;

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
		throws SailConfigException
	{
		super.validate();

		if (url == null) {
			throw new SailConfigException("No URL specified for RdbmsStore");
		}
	}

	@Override
	public Resource export(Model model) {
		Resource implNode = super.export(model);

		ValueFactory vf = new ValueFactoryImpl();

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
		throws SailConfigException
	{
		super.parse(model, implNode);

		try {
			for (Value obj : model.objects(implNode, JDBC_DRIVER)) {
				Literal jdbcDriverLit = (Literal)obj;
				setJdbcDriver(jdbcDriverLit.getLabel());
			}

			for (Value obj : model.objects(implNode, URL)) {
				Literal urlLit = (Literal)obj;
				setUrl(urlLit.getLabel());
			}

			for (Value obj : model.objects(implNode, USER)) {
				Literal userLit = (Literal)obj;
				setUser(userLit.getLabel());
			}

			for (Value obj : model.objects(implNode, PASSWORD)) {
				Literal passwordLit = (Literal)obj;
				setPassword(passwordLit.getLabel());
			}

			for (Value obj : model.objects(implNode, MAX_TRIPLE_TABLES)) {
				Literal maxTripleTablesLit = (Literal)obj;
				try {
					setMaxTripleTables(maxTripleTablesLit.intValue());
				}
				catch (NumberFormatException e) {
					throw new SailConfigException("Invalid value for maxTripleTables: " + maxTripleTablesLit);
				}
			}
		}
		catch (SailConfigException e) {
			throw e;
		}
		catch (Exception e) {
			throw new SailConfigException(e.getMessage(), e);
		}
	}
}
