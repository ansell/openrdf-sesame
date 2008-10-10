/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager.templates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ModelImpl;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.rio.RDFParseException;

/**
 * @author james
 */
public class ConfigTemplate {

	private String id;

	private List<Statement> statements;

	private Model schema;

	public ConfigTemplate(List<Statement> statements, Model schema)
		throws IOException, RDFParseException
	{
		this.schema = schema;
		this.statements = statements;
		for (Statement st : statements) {
			if (st.getPredicate().equals(RepositoryConfigSchema.REPOSITORYID)) {
				id = st.getObject().stringValue();
			}
		}
	}

	public String getID() {
		return id;
	}

	public List<ConfigProperty> getProperties()
		throws IOException
	{
		return getProperties(Locale.getDefault());
	}

	public List<ConfigProperty> getProperties(Locale locale)
		throws IOException
	{
		List<ConfigProperty> properties = new ArrayList<ConfigProperty>();
		for (Statement st : statements) {
			URI pred = st.getPredicate();
			if (schema.contains(pred, null, null)) {
				properties.add(new ConfigProperty(pred, st.getObject(), locale, schema));
			}
		}
		return properties;
	}

	public Model createConfig(List<ConfigProperty> properties) {
		int idx = 0;
		Model model = new ModelImpl();
		for (Statement st : statements) {
			Resource subj = st.getSubject();
			URI pred = st.getPredicate();
			if (schema.contains(pred, null, null)) {
				if (!properties.get(idx).getPredicate().equals(pred))
					throw new IllegalArgumentException("Invalid properties");
				model.add(subj, pred, properties.get(idx++).getValue());
			} else {
				model.add(subj, pred, st.getObject());
			}
		}
		return model;
	}
}
