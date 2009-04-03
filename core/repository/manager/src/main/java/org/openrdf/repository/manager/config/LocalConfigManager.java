/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.store.StoreConfigException;

public class LocalConfigManager implements RepositoryConfigManager {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The directory for configuration files.
	 */
	private final File configurationsDir;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new LocalConfigManager that operates on the specfified
	 * directory.
	 * 
	 * @param configurationsDir
	 *        The directory where the configurations are/must be stored.
	 */
	public LocalConfigManager(File configurationsDir) {
		this.configurationsDir = configurationsDir;
		configurationsDir.mkdirs();
	}

	/*---------*
	 * Methods *
	 *---------*/

	public URL getLocation()
		throws MalformedURLException
	{
		return configurationsDir.toURI().toURL();
	}

	public Set<String> getIDs()
		throws StoreConfigException
	{
		return Collections.unmodifiableSet(getConfigFiles().keySet());
	}

	public Model getConfig(String repositoryID)
		throws StoreConfigException
	{
		File file = getConfigFiles().get(repositoryID);

		if (file == null) {
			return null;
		}

		try {
			RDFFormat format = Rio.getParserFormatForFileName(file.getName());
			RDFParser parser = Rio.createParser(format);

			Model model = new LinkedHashModel();
			parser.setRDFHandler(new StatementCollector(model));

			InputStream stream = new FileInputStream(file);
			try {
				parser.parse(stream, file.toURI().toString());
			}
			catch (RDFHandlerException e) {
				throw new AssertionError(e);
			}
			finally {
				stream.close();
			}

			return model;
		}
		catch (UnsupportedRDFormatException e) {
			throw new StoreConfigException("Unable to parse configuration file " + file.getName()
					+ ", no suitable parser found");
		}
		catch (RDFParseException e) {
			throw new StoreConfigException("Failed to parse configuration file " + file.getName() + ": "
					+ e.getMessage());
		}
		catch (IOException e) {
			throw new StoreConfigException("Failed to read configuration file " + file.getName() + ": "
					+ e.getMessage(), e);
		}
	}

	public void addConfig(String id, Model config)
		throws StoreConfigException
	{
		// Reuse existing config file (if any) and its RDF format
		File file = getConfigFiles().get(id);

		if (file == null) {
			// Store as Turtle by default
			file = new File(configurationsDir, id + "." + RDFFormat.TURTLE.getDefaultFileExtension());
		}

		saveConfig(file, config);
	}

	public boolean removeConfig(String repositoryID)
		throws StoreConfigException
	{
		File file = getConfigFiles().get(repositoryID);

		if (file == null) {
			return false;
		}

		boolean removed = file.delete();
		if (!removed) {
			throw new StoreConfigException("Unable to remove repository configuration file '" + file.getName()
					+ "'");
		}

		return true;
	}

	private Map<String, File> getConfigFiles() {
		Map<String, File> map = new HashMap<String, File>();

		for (File file : configurationsDir.listFiles()) {
			if (file.isFile()) {
				String name = file.getName();
				int extIndex = name.lastIndexOf('.');

				// ignore files that that have no extension or that start with a dot
				if (extIndex >= 1) {
					map.put(name.substring(0, extIndex), file);
				}
			}
		}

		return map;
	}

	private void saveConfig(File file, Model config)
		throws StoreConfigException
	{
		try {
			OutputStream out = new FileOutputStream(file);

			try {
				RDFFormat format = Rio.getWriterFormatForFileName(file.getName());
				RDFWriter writer = Rio.createWriter(format, out);

				writer.startRDF();
				for (Map.Entry<String, String> ns : config.getNamespaces().entrySet()) {
					writer.handleNamespace(ns.getKey(), ns.getValue());
				}
				for (Statement st : config) {
					writer.handleStatement(st);
				}
				writer.endRDF();
			}
			catch (UnsupportedRDFormatException e) {
				throw new StoreConfigException(e);
			}
			catch (RDFHandlerException e) {
				throw new StoreConfigException(e);
			}
			finally {
				out.close();
			}
		}
		catch (IOException e) {
			throw new StoreConfigException(e);
		}
	}
}
