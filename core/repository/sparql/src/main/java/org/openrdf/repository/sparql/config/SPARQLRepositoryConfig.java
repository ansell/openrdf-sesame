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
package org.openrdf.repository.sparql.config;

import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleIRI;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryImplConfigBase;

/**
 * Configuration for a SPARQL endpoint.
 * 
 * @author James Leigh
 */
public class SPARQLRepositoryConfig extends RepositoryImplConfigBase {

	public static final IRI QUERY_ENDPOINT = new SimpleIRI(
			"http://www.openrdf.org/config/repository/sparql#query-endpoint");

	public static final IRI UPDATE_ENDPOINT = new SimpleIRI(
			"http://www.openrdf.org/config/repository/sparql#update-endpoint");

	private String queryEndpointUrl;
	private String updateEndpointUrl;

	public SPARQLRepositoryConfig() {
		super(SPARQLRepositoryFactory.REPOSITORY_TYPE);
	}

	public SPARQLRepositoryConfig(String queryEndpointUrl) {
		setQueryEndpointUrl(queryEndpointUrl);
	}
	
	public SPARQLRepositoryConfig(String queryEndpointUrl, String updateEndpointUrl) {
		this(queryEndpointUrl);
		setUpdateEndpointUrl(updateEndpointUrl);
	}

	public String getQueryEndpointUrl() {
		return queryEndpointUrl;
	}

	public void setQueryEndpointUrl(String url) {
		this.queryEndpointUrl = url;
	}

	public String getUpdateEndpointUrl() {
		return updateEndpointUrl;
	}

	public void setUpdateEndpointUrl(String url) {
		this.updateEndpointUrl = url;
	}
	
	@Override
	public void validate() throws RepositoryConfigException {
		super.validate();
		if (getQueryEndpointUrl() == null) {
			throw new RepositoryConfigException(
					"No endpoint URL specified for SPARQL repository");
		}
	}

	@Override
	public Resource export(Graph graph) {
		Resource implNode = super.export(graph);

		ValueFactory vf = graph.getValueFactory();
		if (getQueryEndpointUrl() != null) {
			graph.add(implNode, QUERY_ENDPOINT, vf.createIRI(getQueryEndpointUrl()));
		}
		if (getUpdateEndpointUrl() != null) {
			graph.add(implNode, UPDATE_ENDPOINT, vf.createIRI(getUpdateEndpointUrl()));
		}

		return implNode;
	}

	@Override
	public void parse(Graph graph, Resource implNode)
			throws RepositoryConfigException {
		super.parse(graph, implNode);

		try {
			IRI uri = GraphUtil.getOptionalObjectURI(graph, implNode, QUERY_ENDPOINT);
			if (uri != null) {
				setQueryEndpointUrl(uri.stringValue());
			}
			
			uri = GraphUtil.getOptionalObjectURI(graph, implNode, UPDATE_ENDPOINT);
			if (uri != null) {
				setUpdateEndpointUrl(uri.stringValue());
			}
		} catch (GraphUtilException e) {
			throw new RepositoryConfigException(e.getMessage(), e);
		}
	}
}
