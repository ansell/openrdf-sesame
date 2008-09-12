/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http.config;

import static org.openrdf.repository.http.config.HTTPRepositorySchema.PASSWORD;
import static org.openrdf.repository.http.config.HTTPRepositorySchema.REPOSITORYURL;
import static org.openrdf.repository.http.config.HTTPRepositorySchema.USERNAME;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
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
	public Resource export(Model model) {
		Resource implNode = super.export(model);

		if (url != null) {
			model.add(implNode, REPOSITORYURL, new ValueFactoryImpl().createURI(url));
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
	public void parse(Model model, Resource implNode)
		throws RepositoryConfigException
	{
		super.parse(model, implNode);

		try {
			for (Value obj : model.objects(implNode, REPOSITORYURL)) {
				URI uri = (URI)obj;
				setURL(uri.toString());
			}
			for (Value obj : model.objects(implNode, USERNAME)) {
				Literal username = (Literal)obj;
				setUsername(username.getLabel());
			}
			for (Value obj : model.objects(implNode, PASSWORD)) {
				Literal password = (Literal)obj;
				setPassword(password.getLabel());
			}
		}
		catch (Exception e) {
			throw new RepositoryConfigException(e.getMessage(), e);
		}
	}
}
