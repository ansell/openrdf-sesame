/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
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
 * @author Arjohn Kampman
 */
public class LocalTemplateManager implements ConfigTemplateManager {

	private static final String REPOSITORY_TEMPLATES = "META-INF/org.openrdf.repository.templates";

	private static final String STORE_SCHEMAS = "META-INF/org.openrdf.store.schemas";

	private final File templateDir;

	private final Logger logger = LoggerFactory.getLogger(LocalTemplateManager.class);

	private final Map<String, ConfigTemplate> templates = new ConcurrentHashMap<String, ConfigTemplate>();

	private ClassLoader cl;

	public LocalTemplateManager() {
		this.templateDir = null;
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

	public Set<String> getIDs()
		throws StoreConfigException
	{
		Set<String> set = new HashSet<String>();
		set.addAll(templates.keySet());
		set.addAll(getTemplateURLs().keySet());
		return set;
	}

	public ConfigTemplate getTemplate(String templateID)
		throws StoreConfigException
	{
		ConfigTemplate template = templates.get(templateID);
		if (template != null) {
			return template;
		}
		URL url = getTemplateURLs().get(templateID);
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

	public void addTemplate(String templateID, Model model)
		throws StoreConfigException
	{
		templates.put(templateID, new ConfigTemplate(model, getSchemas()));
	}

	public boolean removeTemplate(String templateID)
		throws StoreConfigException
	{
		ConfigTemplate template = templates.remove(templateID);
		return template != null;
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

	private String fileNameToID(String fileName) {
		int extIndex = fileName.lastIndexOf('.');

		// ignore files that that have no extension or whose name starts with a
		// dot
		if (extIndex >= 1) {
			return fileName.substring(0, extIndex);
		}

		return null;
	}

	private Map<String, URL> getTemplateURLs()
		throws StoreConfigException
	{
		Map<String, URL> map = new HashMap<String, URL>();
		try {
			map = getTemplateURLs(cl, REPOSITORY_TEMPLATES, map);
			if (templateDir != null && templateDir.isDirectory()) {
				map = getTemplateURLs(templateDir, map);
			}
		}
		catch (IOException e) {
			throw new StoreConfigException(e);
		}
		return map;
	}

	private Map<String, URL> getTemplateURLs(File dir, Map<String, URL> map)
		throws IOException, StoreConfigException
	{
		assert dir.isDirectory();

		for (File file : dir.listFiles()) {
			String id = fileNameToID(file.getName());

			if (id != null) {
				map.put(id, file.toURI().toURL());
			}
		}

		return map;
	}

	private Map<String, URL> getTemplateURLs(ClassLoader cl, String resource, Map<String, URL> map)
		throws IOException, StoreConfigException
	{
		Enumeration<URL> templates = cl.getResources(resource);

		while (templates.hasMoreElements()) {
			Properties prop = new Properties();

			InputStream stream = templates.nextElement().openStream();
			try {
				prop.load(stream);
			}
			finally {
				stream.close();
			}

			Enumeration<?> names = prop.propertyNames();
			while (names.hasMoreElements()) {
				String name = names.nextElement().toString();
				URL url = cl.getResource(name);
				if (url == null) {
					logger.warn("{} not found", name);
					continue;
				}
				File file = new File(URLDecoder.decode(url.getPath(), "UTF-8"));
				String id = fileNameToID(file.getName());
				if (id != null) {
					map.put(id, url);
				}
			}
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

	private static class Collector extends StatementCollector {

		private final URI graph;

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
