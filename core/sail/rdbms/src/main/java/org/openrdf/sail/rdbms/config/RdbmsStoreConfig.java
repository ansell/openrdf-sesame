/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.config;

import static org.openrdf.model.util.GraphUtil.getOptionalObjectLiteral;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.INDEXED;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.JDBC_DRIVER;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.LAYOUT;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.PASSWORD;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.URL;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.USER;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailImplConfigBase;

/**
 * Holds the JDBC Driver, URL, user, and password.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsStoreConfig extends SailImplConfigBase {
	private String jdbcDriver;
	private String url;
	private String user;
	private String password;
	private String layout;
	private String indexed;

	public RdbmsStoreConfig() {
		super(RdbmsStoreFactory.SAIL_TYPE);
	}

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

	public String getLayout() {
		return layout;
	}

	public void setLayout(String layout) {
		this.layout = layout;
	}

	public String getIndexed() {
		return indexed;
	}

	public void setIndexed(String indexed) {
		this.indexed = indexed;
	}

	@Override
	public Resource export(Graph graph) {
		Resource implNode = super.export(graph);
		set(graph, implNode, JDBC_DRIVER, jdbcDriver);
		set(graph, implNode, URL, url);
		set(graph, implNode, USER, user);
		set(graph, implNode, PASSWORD, password);
		set(graph, implNode, LAYOUT, layout);
		set(graph, implNode, INDEXED, indexed);
		return implNode;
	}

	@Override
	public void parse(Graph graph, Resource implNode)
			throws SailConfigException {
		super.parse(graph, implNode);

		try {
			jdbcDriver = get(graph, implNode, JDBC_DRIVER);
			url = get(graph, implNode, URL);
			user = get(graph, implNode, USER);
			password = get(graph, implNode, PASSWORD);
			layout = get(graph, implNode, LAYOUT);
			indexed = get(graph, implNode, INDEXED);
		} catch (GraphUtilException e) {
			throw new SailConfigException(e.getMessage(), e);
		}
	}

	private String get(Graph graph, Resource implNode, URI predicate)
			throws GraphUtilException {
		Literal lit = getOptionalObjectLiteral(graph, implNode, predicate);
		if (lit == null)
			return null;
		return lit.getLabel();
	}

	private void set(Graph graph, Resource implNode, URI predicate, String value) {
		if (value != null) {
			Literal lit = graph.getValueFactory().createLiteral(value);
			graph.add(implNode, predicate, lit);
		}
	}

}
