/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sparql.config;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
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

	public static final URI ENDPOINT = new URIImpl(
			"http://www.openrdf.org/config/repository/sparql#endpoint");

	public static final URI SUBJECT_SPACE = new URIImpl(
			"http://www.openrdf.org/config/repository/sparql#subjectSpace");

	private String url;

	private Set<String> subjects = new HashSet<String>();

	public SPARQLRepositoryConfig() {
		super(SPARQLRepositoryFactory.REPOSITORY_TYPE);
	}

	public SPARQLRepositoryConfig(String url) {
		setURL(url);
	}

	public String getURL() {
		return url;
	}

	public void setURL(String url) {
		this.url = url;
	}

	public Set<String> getSubjectSpaces() {
		return subjects;
	}

	public void setSubjectSpaces(Set<String> subjects) {
		this.subjects = subjects;
	}

	@Override
	public void validate() throws RepositoryConfigException {
		super.validate();
		if (url == null) {
			throw new RepositoryConfigException(
					"No URL specified for SPARQL repository");
		}
	}

	@Override
	public Resource export(Graph graph) {
		Resource implNode = super.export(graph);

		ValueFactory vf = graph.getValueFactory();
		if (url != null) {
			graph.add(implNode, ENDPOINT, vf.createURI(url));
		}
		for (String space : subjects) {
			graph.add(implNode, SUBJECT_SPACE, vf.createURI(space));
		}

		return implNode;
	}

	@Override
	public void parse(Graph graph, Resource implNode)
			throws RepositoryConfigException {
		super.parse(graph, implNode);

		try {
			URI uri = GraphUtil.getOptionalObjectURI(graph, implNode, ENDPOINT);
			if (uri != null) {
				setURL(uri.stringValue());
			}
			Set<Value> set = GraphUtil.getObjects(graph, implNode,
					SUBJECT_SPACE);
			subjects.clear();
			for (Value space : set) {
				subjects.add(space.stringValue());
			}
		} catch (GraphUtilException e) {
			throw new RepositoryConfigException(e.getMessage(), e);
		}
	}
}
