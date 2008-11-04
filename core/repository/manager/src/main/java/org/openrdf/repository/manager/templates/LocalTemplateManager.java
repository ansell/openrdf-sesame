/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager.templates;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.store.StoreConfigException;

/**
 * @author james
 */
public class LocalTemplateManager implements ConfigTemplateManager {

	private static final String REPOSITORY_TEMPLATES = "META-INF/org.openrdf.repository.templates";

	private static final String STORE_SCHEMAS = "META-INF/org.openrdf.store.schemas";

	private File templateDir;

	private Logger logger = LoggerFactory.getLogger(LocalTemplateManager.class);

	private Model schema = new ModelImpl();

	private Map<String, ConfigTemplate> services = new ConcurrentHashMap<String, ConfigTemplate>();

	private ClassLoader cl;

	public LocalTemplateManager() {
	}

	public LocalTemplateManager(File templateDir) {
		this.templateDir = templateDir;
	}

	public void setClassLoader(ClassLoader cl) {
		this.cl = cl;
	}

	public void init()
		throws StoreConfigException
	{
		if (cl == null) {
			cl = Thread.currentThread().getContextClassLoader();
		}
		try {
			loadTemplates(cl);
			if (templateDir != null && templateDir.isDirectory()) {
				loadTemplates(templateDir);
			}
		}
		catch (IOException e) {
			throw new StoreConfigException(e);
		}
	}

	public URL getLocation()
		throws MalformedURLException
	{
		return templateDir.toURI().toURL();
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
	 * @throws StoreConfigException
	 */
	public void addTemplate(ConfigTemplate service)
		throws StoreConfigException
	{
		String id = service.getID();
		if (services.containsKey(id)) {
			throw new StoreConfigException("Template already exists");
		}
		services.put(id, service);
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
	 * @throws StoreConfigException
	 */
	public void updateTemplate(ConfigTemplate service)
		throws StoreConfigException
	{
		String id = service.getID();
		if (!services.containsKey(id)) {
			throw new StoreConfigException("Template does not exists");
		}
		services.put(id, service);
	}

	/**
	 * Removes a service from the registry.
	 * 
	 * @param service
	 *        The service be removed from the registry.
	 * @throws StoreConfigException
	 */
	public void removeTemplate(String id)
		throws StoreConfigException
	{
		if (services.remove(id) == null) {
			throw new StoreConfigException("Template does not exists");
		}
	}

	/**
	 * Gets the service for the specified key, if any.
	 * 
	 * @param key
	 *        The key identifying which service to get.
	 * @return The service for the specified key, or <tt>null</tt> if no such
	 *         service is avaiable.
	 */
	public ConfigTemplate getTemplate(String key) {
		return services.get(key);
	}

	/**
	 * Gets the set of registered keys.
	 * 
	 * @return An unmodifiable set containing all registered keys.
	 */
	public Set<String> getIDs() {
		return Collections.unmodifiableSet(services.keySet());
	}

	private void loadTemplates(ClassLoader cl)
		throws IOException, StoreConfigException, AssertionError
	{
		loadResources(schema, cl, STORE_SCHEMAS);
		loadTemplates(cl, REPOSITORY_TEMPLATES);
	}

	private void loadTemplates(File dir)
		throws IOException, StoreConfigException
	{
		assert dir.isDirectory();
		for (File file : dir.listFiles()) {
			try {
				addTemplate(new ConfigTemplate(parse(file.toURI().toURL()), schema));
			}
			catch (RDFParseException e) {
				logger.warn(e.toString(), e);
				continue;
			}
		}
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

				RDFFormat format = Rio.getParserFormatForFileName(url.getFile());
				if (format == null) {
					logger.warn("Unsupported RDF format: {}", url.getFile());
					continue;
				}

				try {
					RDFParser parser = Rio.createParser(format);
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
				catch (UnsupportedRDFormatException e) {
					logger.warn("Unable to parse template file", e);
				}
			}
		}
	}

	private void loadTemplates(ClassLoader cl, String resource)
		throws IOException, AssertionError, StoreConfigException
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
					addTemplate(new ConfigTemplate(parse(url), schema));
				}
				catch (RDFParseException e) {
					logger.warn(e.toString(), e);
					continue;
				}
			}

		}
	}

	private List<Statement> parse(URL url)
		throws IOException, RDFParseException
	{
		RDFFormat format = Rio.getParserFormatForFileName(url.getFile());
		if (format == null) {
			throw new IOException("Unsupported file format: " + url.getFile());
		}

		try {
			RDFParser parser = Rio.createParser(format);

			List<Statement> statements = new ArrayList<Statement>();
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
		catch (UnsupportedRDFormatException e) {
			IOException ioe = new IOException("Unsupported file format: " + url.getFile());
			ioe.initCause(e);
			throw ioe;
		} catch (RDFParseException e) {
			e.setFilename(url.toString());
			throw e;
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
