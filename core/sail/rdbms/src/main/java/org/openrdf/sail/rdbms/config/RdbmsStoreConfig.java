/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.config;

import static org.openrdf.model.util.GraphUtil.getOptionalObjectLiteral;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.JDBC_DRIVER;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.LAYOUT;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.PASSWORD;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.URL;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.USER;

import java.util.Locale;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailImplConfigBase;

/**
 * Holds the JDBC Driver, URL, user and password, as well as the database
 * layout.
 * 
 * @author James Leigh
 */
public class RdbmsStoreConfig extends SailImplConfigBase {

	public enum DatabaseLayout {
		MONOLITHIC,
		VERTICAL
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	private String jdbcDriver;

	private String url;

	private String user;

	private String password;

	private DatabaseLayout layout = DatabaseLayout.VERTICAL;

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

	public DatabaseLayout getLayout() {
		return layout;
	}

	public void setLayout(DatabaseLayout layout) {
		this.layout = layout;
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
	public Resource export(Graph graph) {
		Resource implNode = super.export(graph);

		ValueFactory vf = graph.getValueFactory();

		if (jdbcDriver != null) {
			graph.add(implNode, JDBC_DRIVER, vf.createLiteral(jdbcDriver));
		}
		if (url != null) {
			graph.add(implNode, URL, vf.createLiteral(url));
		}
		if (user != null) {
			graph.add(implNode, USER, vf.createLiteral(user));
		}
		if (password != null) {
			graph.add(implNode, PASSWORD, vf.createLiteral(password));
		}
		if (layout != null) {
			graph.add(implNode, LAYOUT, vf.createLiteral(layout.toString()));
		}

		return implNode;
	}

	@Override
	public void parse(Graph graph, Resource implNode)
		throws SailConfigException
	{
		super.parse(graph, implNode);

		try {
			Literal jdbcDriverLit = getOptionalObjectLiteral(graph, implNode, JDBC_DRIVER);
			if (jdbcDriverLit != null) {
				setJdbcDriver(jdbcDriverLit.getLabel());
			}

			Literal urlLit = getOptionalObjectLiteral(graph, implNode, URL);
			if (urlLit != null) {
				setUrl(urlLit.getLabel());
			}

			Literal userLit = getOptionalObjectLiteral(graph, implNode, USER);
			if (userLit != null) {
				setUser(userLit.getLabel());
			}

			Literal passwordLit = getOptionalObjectLiteral(graph, implNode, PASSWORD);
			if (passwordLit != null) {
				setPassword(passwordLit.getLabel());
			}

			Literal layoutLit = getOptionalObjectLiteral(graph, implNode, LAYOUT);
			if (layoutLit != null) {
				String layoutName = layoutLit.getLabel().toUpperCase(Locale.ENGLISH);
				try {
					setLayout(DatabaseLayout.valueOf(layoutName));
				}
				catch (IllegalArgumentException e) {
					throw new SailConfigException("Invalid database layout value: " + layoutName);
				}
			}
		}
		catch (GraphUtilException e) {
			throw new SailConfigException(e.getMessage(), e);
		}
	}
}
