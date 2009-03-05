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
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
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
import org.openrdf.model.impl.LinkedHashModel;
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
 * @author James Leigh
 */
public class LocalTemplateManager implements ConfigTemplateManager {

	private static final String REPOSITORY_TEMPLATES = "META-INF/org.openrdf.repository.templates";

	private static final String STORE_SCHEMAS = "META-INF/org.openrdf.store.schemas";

	private File templateDir;

	private Logger logger = LoggerFactory.getLogger(LocalTemplateManager.class);

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
	public void addTemplate(String id, Model model)
		throws StoreConfigException
	{
		services.put(id, new ConfigTemplate(model, getSchemas()));
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
			throw new StoreConfigException("Template does not exists or cannot be removed");
		}
	}

	/**
	 * Gets the service for the specified key, if any.
	 * 
	 * @param key
	 *        The key identifying which service to get.
	 * @return The service for the specified key, or <tt>null</tt> if no such
	 *         service is avaiable.
	 * @throws StoreConfigException
	 */
	public ConfigTemplate getTemplate(String key)
		throws StoreConfigException
	{
		ConfigTemplate template = services.get(key);
		if (template != null) {
			return template;
		}
		URL url = loadTemplates().get(key);
		if (url == null) {
			return null;
		}
		try {
			return new ConfigTemplate(parse(url), getSchemas());
		}
		catch (RDFParseException e) {
			throw new StoreConfigException(e);
		}
		catch (IOException e) {
			throw new StoreConfigException(e);
		}
	}

	/**
	 * Gets the set of registered keys.
	 * 
	 * @return An unmodifiable set containing all registered keys.
	 * @throws StoreConfigException
	 */
	public Set<String> getIDs()
		throws StoreConfigException
	{
		Set<String> set = new HashSet<String>();
		set.addAll(services.keySet());
		set.addAll(loadTemplates().keySet());
		return set;
	}

	public Model getSchemas()
		throws StoreConfigException
	{
		try {
			return loadResources(new LinkedHashModel(), cl, STORE_SCHEMAS);
		}
		catch (IOException e) {
			throw new StoreConfigException(e);
		}
	}

	private Map<String, URL> loadTemplates()
		throws StoreConfigException
	{
		Map<String, URL> map = new HashMap<String, URL>();
		try {
			map = loadTemplates(cl, REPOSITORY_TEMPLATES, map);
			if (templateDir != null && templateDir.isDirectory()) {
				map = loadTemplates(templateDir, map);
			}
		}
		catch (IOException e) {
			throw new StoreConfigException(e);
		}
		return map;
	}

	private Map<String, URL> loadTemplates(File dir, Map<String, URL> map)
		throws IOException, StoreConfigException
	{
		assert dir.isDirectory();
		for (File file : dir.listFiles()) {
			String id = file.getName();
			if (id.indexOf('.') > 0) {
				id = id.substring(0, id.indexOf('.'));
			}
			map.put(id, file.toURI().toURL());
		}
		return map;
	}

	private Model loadResources(Model model, ClassLoader cl, String resource)
		throws IOException
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
		return model;
	}

	private Map<String, URL> loadTemplates(ClassLoader cl, String resource, Map<String, URL> map)
		throws IOException, StoreConfigException
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
				String id = new File(URLDecoder.decode(url.getPath(), "UTF-8")).getName();
				if (id.indexOf('.') > 0) {
					id = id.substring(0, id.indexOf('.'));
				}
				map.put(id, url);
			}

		}
		return map;
	}

	private Model parse(URL url)
		throws IOException, RDFParseException
	{
		RDFFormat format = Rio.getParserFormatForFileName(url.getFile());
		if (format == null) {
			throw new IOException("Unsupported file format: " + url.getFile());
		}

		try {
			RDFParser parser = Rio.createParser(format);

			Model statements = new LinkedHashModel();
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
		}
		catch (RDFParseException e) {
			e.setFilename(url.toString());
			throw e;
		}
	}

	private class Collector extends StatementCollector {

		private URI graph;

		public Collector(Model statements, String graph) {
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
