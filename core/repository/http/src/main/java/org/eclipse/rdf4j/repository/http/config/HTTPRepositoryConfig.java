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
package org.eclipse.rdf4j.repository.http.config;

import static org.eclipse.rdf4j.repository.http.config.HTTPRepositorySchema.PASSWORD;
import static org.eclipse.rdf4j.repository.http.config.HTTPRepositorySchema.REPOSITORYURL;
import static org.eclipse.rdf4j.repository.http.config.HTTPRepositorySchema.USERNAME;

import org.eclipse.rdf4j.model.Graph;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.GraphUtil;
import org.eclipse.rdf4j.model.util.GraphUtilException;
import org.eclipse.rdf4j.model.util.ModelException;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.repository.config.AbstractRepositoryImplConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;

/**
 * @author Arjohn Kampman
 */
public class HTTPRepositoryConfig extends AbstractRepositoryImplConfig {

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
	public Resource export(Model graph) {
		Resource implNode = super.export(graph);

		if (url != null) {
			graph.add(implNode, REPOSITORYURL, SimpleValueFactory.getInstance().createIRI(url));
		}
		// if (username != null) {
		// graph.add(implNode, USERNAME,
		// graph.getValueFactory().createLiteral(username));
		// }
		// if (password != null) {
		// graph.add(implNode, PASSWORD,
		// graph.getValueFactory().createLiteral(password));
		// }

		return implNode;
	}

	@Override
	public void parse(Model model, Resource implNode)
		throws RepositoryConfigException
	{
		super.parse(model, implNode);

		try {
			Models.objectIRI(model.filter(implNode, REPOSITORYURL, null)).ifPresent(
					iri -> setURL(iri.stringValue()));

			Models.objectLiteral(model.filter(implNode, USERNAME, null)).ifPresent(
					username -> setUsername(username.getLabel()));

			Models.objectLiteral(model.filter(implNode, PASSWORD, null)).ifPresent(
					password -> setPassword(password.getLabel()));

		}
		catch (ModelException e) {
			throw new RepositoryConfigException(e.getMessage(), e);
		}
	}
}
