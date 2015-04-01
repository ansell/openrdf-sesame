/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.repository.http.config;

import static org.openrdf.repository.http.config.HTTPRepositorySchema.PASSWORD;
import static org.openrdf.repository.http.config.HTTPRepositorySchema.REPOSITORYURL;
import static org.openrdf.repository.http.config.HTTPRepositorySchema.USERNAME;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.IRI;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryImplConfigBase;

/**
 * @author Arjohn Kampman
 */
public class HTTPRepositoryConfig extends RepositoryImplConfigBase {

	private String url;

	private String username;

	private String password;

	public HTTPRepositoryConfig() {
		super(HTTPRepositoryFactory.REPOSITORY_TYPE);
	}

	public HTTPRepositoryConfig(String url) {
		this();
		setURL(url);
	}

	public String getURL() {
		return url;
	}

	public void setURL(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public void validate()
		throws RepositoryConfigException
	{
		super.validate();
		if (url == null) {
			throw new RepositoryConfigException("No URL specified for HTTP repository");
		}
	}

	@Override
	public Resource export(Graph graph) {
		Resource implNode = super.export(graph);

		if (url != null) {
			graph.add(implNode, REPOSITORYURL, graph.getValueFactory().createIRI(url));
		}
//		if (username != null) {
//			graph.add(implNode, USERNAME, graph.getValueFactory().createLiteral(username));
//		}
//		if (password != null) {
//			graph.add(implNode, PASSWORD, graph.getValueFactory().createLiteral(password));
//		}

		return implNode;
	}

	@Override
	public void parse(Graph graph, Resource implNode)
		throws RepositoryConfigException
	{
		super.parse(graph, implNode);

		try {
			IRI uri = GraphUtil.getOptionalObjectURI(graph, implNode, REPOSITORYURL);
			if (uri != null) {
				setURL(uri.toString());
			}
			Literal username = GraphUtil.getOptionalObjectLiteral(graph, implNode, USERNAME);
			if (username != null) {
				setUsername(username.getLabel());
			}
			Literal password = GraphUtil.getOptionalObjectLiteral(graph, implNode, PASSWORD);
			if (password != null) {
				setPassword(password.getLabel());
			}
		}
		catch (GraphUtilException e) {
			throw new RepositoryConfigException(e.getMessage(), e);
		}
	}
}
