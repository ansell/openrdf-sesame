/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ModelImpl;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.helpers.StatementCollector;

/**
 * @author james
 */
public class ConfigTemplate {

	private String id;

	private List<Statement> statements;

	private Model schema;

	public ConfigTemplate(URL url, Model schema)
		throws IOException, RDFParseException
	{
		this.schema = schema;
		this.statements = parse(url);
		for (Statement st : statements) {
			if (st.getPredicate().equals(RepositoryConfigSchema.REPOSITORYID)) {
				id = st.getObject().stringValue();
			}
		}
	}

	public String getId() {
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

	public Model createConfig(Map<URI, Literal> map) {
		Model model = new ModelImpl();
		for (Statement st : statements) {
			Resource subj = st.getSubject();
			URI pred = st.getPredicate();
			if (map.containsKey(pred)) {
				model.add(subj, pred, map.get(pred));
			} else {
				model.add(subj, pred, st.getObject());
			}
		}
		return model;
	}

	private List<Statement> parse(URL url)
		throws IOException, RDFParseException
	{
		RDFFormat format = RDFFormat.forFileName(url.getFile());
		if (format == null) {
			throw new IOException("Unknown file format: " + url.getFile());
		}
		List<Statement> statements = new ArrayList<Statement>();
		RDFParserRegistry parsers = RDFParserRegistry.getInstance();
		RDFParser parser = parsers.get(format).getParser();
		parser.setRDFHandler(new StatementCollector(statements));
		InputStream stream = url.openStream();
		try {
			parser.parse(stream, url.toString());
		}
		catch (RDFHandlerException e) {
			throw new AssertionError(e);
		}
		finally {
			stream.close();
		}
		return statements;
	}
}
