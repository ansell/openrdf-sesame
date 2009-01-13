/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http.config;

import static org.openrdf.repository.http.config.HTTPRepositorySchema.*;
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
import org.openrdf.model.util.ModelException;
import org.openrdf.repository.config.RepositoryImplConfigBase;
import org.openrdf.store.StoreConfigException;

/**
 * @author Arjohn Kampman
 */
public class HTTPRepositoryConfig extends RepositoryImplConfigBase {

	private String url;

	private String username;

	private String password;

	private boolean readOnly;

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

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
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
		if (readOnly) {
			model.add(implNode, READ_ONLY, vf.createLiteral(readOnly));
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
			Value server = model.filter(implNode, SERVERURL, null).objectValue();
			Literal id = model.filter(implNode, REPOSITORYID, null).objectLiteral();
			if (server != null && id != null) {
				setURL(server.stringValue() + "/repositories/" + id.stringValue());
			}
			URI uri = model.filter(implNode, REPOSITORYURL, null).objectURI();
			if (uri != null) {
				setURL(uri.toString());
			}
			Literal username = model.filter(implNode, USERNAME, null).objectLiteral();
			if (username != null) {
				setUsername(username.getLabel());
			}
			Literal password = model.filter(implNode, PASSWORD, null).objectLiteral();
			if (password != null) {
				setPassword(password.getLabel());
			}
			Literal readOnly = model.filter(implNode, READ_ONLY, null).objectLiteral();
			if (readOnly != null) {
				setReadOnly(readOnly.booleanValue());
			}
			for (Value obj : model.filter(implNode, SUBJECTSPACE, null).objects()) {
				subjectSpace.add(obj.stringValue());
			}
		}
		catch (ModelException e) {
			throw new StoreConfigException(e.getMessage(), e);
		}
	}
}
