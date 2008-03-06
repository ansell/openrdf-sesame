/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.config;

import static org.openrdf.sail.config.SailConfigSchema.SAILTYPE;

import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;

/**
 * @author Herko ter Horst
 */
public class SailImplConfigBase implements SailImplConfig {

	private String type;

	/**
	 * Create a new RepositoryConfigImpl.
	 */
	public SailImplConfigBase() {
	}

	/**
	 * Create a new RepositoryConfigImpl.
	 */
	public SailImplConfigBase(String type) {
		this();
		setType(type);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void validate()
		throws SailConfigException
	{
		if (type == null) {
			throw new SailConfigException("No type specified for repository implementation");
		}
	}

	public Resource export(Graph graph) {
		BNode implNode = graph.getValueFactory().createBNode();

		if (type != null) {
			graph.add(implNode, SAILTYPE, graph.getValueFactory().createLiteral(type));
		}

		return implNode;
	}

	public void parse(Graph graph, Resource implNode)
		throws SailConfigException
	{
		try {
			Literal typeLit = GraphUtil.getOptionalObjectLiteral(graph, implNode, SAILTYPE);
			if (typeLit != null) {
				setType(typeLit.getLabel());
			}
		}
		catch (GraphUtilException e) {
			throw new SailConfigException(e.getMessage(), e);
		}
	}
}
