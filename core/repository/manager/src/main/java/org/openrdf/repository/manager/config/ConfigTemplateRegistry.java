/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ModelImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.helpers.StatementCollector;

/**
 * @author james
 */
public class ConfigTemplateRegistry {

	private static final String REPOSITORY_TEMPLATES = "META-INF/org.openrdf.repository.templates";

	private static final String STORE_SCHEMAS = "META-INF/org.openrdf.store.schemas";

	private static ConfigTemplateRegistry instance;
	static {
		try {
			instance = new ConfigTemplateRegistry();
		}
		catch (Exception e) {
			Logger logger = LoggerFactory.getLogger(ConfigTemplateRegistry.class);
			logger.error("Failed to instantiate service", e);
		}
	}

	public static ConfigTemplateRegistry getInstance() {
		return instance;
	}

	private Logger logger = LoggerFactory.getLogger(ConfigTemplateRegistry.class);

	private Model schema = new ModelImpl();

	private Map<String, ConfigTemplate> services = new HashMap<String, ConfigTemplate>();

	public ConfigTemplateRegistry()
		throws IOException
	{
		loadTemplates(Thread.currentThread().getContextClassLoader());
	}

	public void loadTemplates(ClassLoader cl)
		throws IOException
	{
		loadResources(schema, cl, STORE_SCHEMAS);
		loadTemplates(cl, REPOSITORY_TEMPLATES);
	}

	public void loadTemplates(File dir)
		throws IOException
	{
		assert dir.isDirectory();
		for (File file : dir.listFiles()) {
			try {
				add(new ConfigTemplate(file.toURI().toURL(), schema));
			}
			catch (RDFParseException e) {
				logger.warn(e.toString(), e);
				continue;
			}
		}
	}

	/**
	 * Adds a service to the registry. Any service that is currently registered
	 * for the same key (as specified by {@link #getKey(Object)}) will be
	 * replaced with the new service.
	 * 
	 * @param service
	 *        The service that should be added to the registry.
	 * @return The previous service that was registered for the same key, or
	 *         <tt>null</tt> if there was no such service.
	 */
	public ConfigTemplate add(ConfigTemplate service) {
		return services.put(getKey(service), service);
	}

	/**
	 * Removes a service from the registry.
	 * 
	 * @param service
	 *        The service be removed from the registry.
	 */
	public ConfigTemplate remove(String key) {
		return services.remove(key);
	}

	/**
	 * Gets the service for the specified key, if any.
	 * 
	 * @param key
	 *        The key identifying which service to get.
	 * @return The service for the specified key, or <tt>null</tt> if no such
	 *         service is avaiable.
	 */
	public ConfigTemplate get(String key) {
		return services.get(key);
	}

	/**
	 * Checks whether a service for the specified key is available.
	 * 
	 * @param key
	 *        The key identifying which service to search for.
	 * @return <tt>true</tt> if a service for the specific key is available,
	 *         <tt>false</tt> otherwise.
	 */
	public boolean has(String key) {
		return services.containsKey(key);
	}

	/**
	 * Gets the set of registered keys.
	 * 
	 * @return An unmodifiable set containing all registered keys.
	 */
	public Set<String> getKeys() {
		return Collections.unmodifiableSet(services.keySet());
	}

	/**
	 * Gets the key for the specified service.
	 * 
	 * @param service
	 *        The service to get the key for.
	 * @return The key for the specified service.
	 */
	private String getKey(ConfigTemplate template) {
		return template.getId();
	}

	private void loadResources(Model model, ClassLoader cl, String resource)
		throws IOException, AssertionError
	{
		Enumeration<URL> schemas = cl.getResources(resource);
		while (schemas.hasMoreElements()) {
			Properties prop = new Properties();
			prop.load(schemas.nextElement().openStream());
			Enumeration<?> names = prop.propertyNames();
			while (names.hasMoreElements()) {
				String name = names.nextElement().toString();
				URL url = cl.getResource(name);
				if (url == null) {
					logger.warn("{} not found", name);
					continue;
				}
				RDFFormat format = RDFFormat.forFileName(url.getFile());
				if (format == null) {
					logger.warn("Unkown RDF format {}", url.getFile());
					continue;
				}
				RDFParserRegistry parsers = RDFParserRegistry.getInstance();
				RDFParser parser = parsers.get(format).getParser();
				parser.setRDFHandler(new Collector(model, url.toString()));
				InputStream stream = url.openStream();
				try {
					parser.parse(stream, url.toString());
				}
				catch (RDFParseException e) {
					logger.warn(e.toString(), e);
					continue;
				}
				catch (RDFHandlerException e) {
					throw new AssertionError(e);
				}
				finally {
					stream.close();
				}
			}
		}
	}

	private void loadTemplates(ClassLoader cl, String resource)
		throws IOException, AssertionError
	{
		Enumeration<URL> templates = cl.getResources(resource);
		while (templates.hasMoreElements()) {
			Properties prop = new Properties();
			prop.load(templates.nextElement().openStream());
			Enumeration<?> names = prop.propertyNames();
			while (names.hasMoreElements()) {
				String name = names.nextElement().toString();
				URL url = cl.getResource(name);
				if (url == null) {
					logger.warn("{} not found", name);
					continue;
				}
				try {
					add(new ConfigTemplate(url, schema));
				}
				catch (RDFParseException e) {
					logger.warn(e.toString(), e);
					continue;
				}
			}
			
		}
	}

	private class Collector extends StatementCollector {

		private URI graph;

		public Collector(Collection<Statement> statements, String graph) {
			super(statements);
			this.graph = new URIImpl(graph);
		}

		@Override
		public void handleStatement(Statement st) {
			Resource s = st.getSubject();
			URI p = st.getPredicate();
			Value o = st.getObject();
			super.handleStatement(new StatementImpl(s, p, o, graph));
		}

	}
}
