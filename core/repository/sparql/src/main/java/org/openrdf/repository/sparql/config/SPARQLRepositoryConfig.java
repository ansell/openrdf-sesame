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
package org.openrdf.repository.sparql.config;

import org.openrdf.model.IRI;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleIRI;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.util.ModelException;
import org.openrdf.model.util.Models;
import org.openrdf.repository.config.AbstractRepositoryImplConfig;
import org.openrdf.repository.config.RepositoryConfigException;

/**
 * Configuration for a SPARQL endpoint.
 * 
 * @author James Leigh
 */
public class SPARQLRepositoryConfig extends AbstractRepositoryImplConfig {

	private static final ValueFactory vf = SimpleValueFactory.getInstance();

	public static final IRI QUERY_ENDPOINT = vf.createIRI(
			"http://www.openrdf.org/config/repository/sparql#query-endpoint");

	public static final IRI UPDATE_ENDPOINT = vf.createIRI(
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
	public void validate()
		throws RepositoryConfigException
	{
		super.validate();
		if (getQueryEndpointUrl() == null) {
			throw new RepositoryConfigException("No endpoint URL specified for SPARQL repository");
		}
	}

	@Override
	public Resource export(Model m) {
		Resource implNode = super.export(m);

		if (getQueryEndpointUrl() != null) {
			m.add(implNode, QUERY_ENDPOINT, vf.createIRI(getQueryEndpointUrl()));
		}
		if (getUpdateEndpointUrl() != null) {
			m.add(implNode, UPDATE_ENDPOINT, vf.createIRI(getUpdateEndpointUrl()));
		}

		return implNode;
	}

	@Override
	public void parse(Model m, Resource implNode)
		throws RepositoryConfigException
	{
		super.parse(m, implNode);

		try {
			Models.objectIRI(m.filter(implNode, QUERY_ENDPOINT, null)).ifPresent(
					iri -> setQueryEndpointUrl(iri.stringValue()));
			Models.objectIRI(m.filter(implNode, UPDATE_ENDPOINT, null)).ifPresent(
					iri -> setUpdateEndpointUrl(iri.stringValue()));
		}
		catch (ModelException e) {
			throw new RepositoryConfigException(e.getMessage(), e);
		}
	}
}
