/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.config;

import static org.openrdf.sail.config.SailConfigSchema.SAILTYPE;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;

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
			throw new SailConfigException("No type specified for sail implementation");
		}
	}

	public Resource export(Model model) {
		ValueFactoryImpl vf = new ValueFactoryImpl();
		BNode implNode = vf.createBNode();

		if (type != null) {
			model.add(implNode, SAILTYPE, vf.createLiteral(type));
		}

		return implNode;
	}

	public void parse(Model model, Resource implNode)
		throws SailConfigException
	{
		try {
			for (Value obj : model.objects(implNode, SAILTYPE)) {
				Literal typeLit = (Literal)obj;
				setType(typeLit.getLabel());
			}
		}
		catch (Exception e) {
			throw new SailConfigException(e.getMessage(), e);
		}
	}
}
