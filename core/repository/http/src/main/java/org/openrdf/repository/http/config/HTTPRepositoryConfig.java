/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http.config;

import static org.openrdf.repository.http.config.HTTPRepositorySchema.PASSWORD;
import static org.openrdf.repository.http.config.HTTPRepositorySchema.REPOSITORYID;
import static org.openrdf.repository.http.config.HTTPRepositorySchema.REPOSITORYURL;
import static org.openrdf.repository.http.config.HTTPRepositorySchema.SERVERURL;
import static org.openrdf.repository.http.config.HTTPRepositorySchema.SUBJECTSPACE;
import static org.openrdf.repository.http.config.HTTPRepositorySchema.USERNAME;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.model.util.ModelUtilException;
import org.openrdf.repository.config.RepositoryImplConfigBase;
import org.openrdf.store.StoreConfigException;

/**
 * @author Arjohn Kampman
 */
public class HTTPRepositoryConfig extends RepositoryImplConfigBase {

	private String url;

	private String username;

	private String password;

	private Set<String> subjectSpace = new HashSet<String>();

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

	public Set<String> getSubjectSpace() {
		return subjectSpace;
	}

	public void setSubjectSpace(Set<String> subjectSpace) {
		this.subjectSpace = new HashSet<String>(subjectSpace);
	}

	@Override
	public void validate()
		throws StoreConfigException
	{
		super.validate();
		if (url == null) {
			throw new StoreConfigException("No URL specified for HTTP repository");
		}
	}

	@Override
	public Resource export(Model model) {
		Resource implNode = super.export(model);
		ValueFactoryImpl vf = ValueFactoryImpl.getInstance();

		if (url != null) {
			model.add(implNode, REPOSITORYURL, vf.createURI(url));
		}
		for (String space : subjectSpace) {
			model.add(implNode, SUBJECTSPACE, vf.createURI(space));
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
		throws StoreConfigException
	{
		super.parse(model, implNode);

		try {
			Value server = ModelUtil.getOptionalObject(model, implNode, SERVERURL);
			Literal id = ModelUtil.getOptionalObjectLiteral(model, implNode, REPOSITORYID);
			if (server != null && id != null) {
				setURL(server.stringValue() + "/repositories/" + id.stringValue());
			}
			URI uri = ModelUtil.getOptionalObjectURI(model, implNode, REPOSITORYURL);
			if (uri != null) {
				setURL(uri.toString());
			}
			Literal username = ModelUtil.getOptionalObjectLiteral(model, implNode, USERNAME);
			if (username != null) {
				setUsername(username.getLabel());
			}
			Literal password = ModelUtil.getOptionalObjectLiteral(model, implNode, PASSWORD);
			if (password != null) {
				setPassword(password.getLabel());
			}
			for (Value obj : model.filter(implNode, SUBJECTSPACE, null).objects()) {
				subjectSpace.add(obj.stringValue());
			}
		}
		catch (ModelUtilException e) {
			throw new StoreConfigException(e.getMessage(), e);
		}
	}
}
