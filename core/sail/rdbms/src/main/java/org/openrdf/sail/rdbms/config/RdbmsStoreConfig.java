/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.openrdf.sail.rdbms.config;

import static org.openrdf.model.util.GraphUtil.getOptionalObjectLiteral;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.JDBC_DRIVER;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.MAX_TRIPLE_TABLES;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.PASSWORD;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.URL;
import static org.openrdf.sail.rdbms.config.RdbmsStoreSchema.USER;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.AbstractSailImplConfig;

/**
 * Holds the JDBC Driver, URL, user and password, as well as the database
 * layout.
 * 
 * @author James Leigh
 */
public class RdbmsStoreConfig extends AbstractSailImplConfig {

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
		graph.add(implNode, MAX_TRIPLE_TABLES, vf.createLiteral(maxTripleTables));

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

			Literal maxTripleTablesLit = getOptionalObjectLiteral(graph, implNode, MAX_TRIPLE_TABLES);
			if (maxTripleTablesLit != null) {
				try {
					setMaxTripleTables(maxTripleTablesLit.intValue());
				}
				catch (NumberFormatException e) {
					throw new SailConfigException("Invalid value for maxTripleTables: " + maxTripleTablesLit);
				}
			}
		}
		catch (GraphUtilException e) {
			throw new SailConfigException(e.getMessage(), e);
		}
	}
}
