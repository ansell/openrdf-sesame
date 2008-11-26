/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.ModelImpl;
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
	 * The base dir to resolve any relative paths against.
	 */
	private File baseDir;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new RepositoryManager that operates on the specfified base
	 * directory.
	 * 
	 * @param baseDir
	 *        The base directory where data for repositories can be stored, among
	 *        other things.
	 */
	public LocalConfigManager(File baseDir) {
		this.baseDir = baseDir;
		baseDir.mkdirs();
	}

	/*---------*
	 * Methods *
	 *---------*/

	public URL getLocation()
		throws MalformedURLException
	{
		return baseDir.toURI().toURL();
	}

	public Set<String> getIDs()
		throws StoreConfigException
	{
		return getRdfFiles().keySet();
	}

	public Model getConfig(String repositoryID)
		throws StoreConfigException
	{
		File file = getRdfFiles().get(repositoryID);
		if (file == null) {
			throw new StoreConfigException("No such repository config");
		}

		RDFFormat format = Rio.getParserFormatForFileName(file.getName());
		if (format == null) {
			throw new StoreConfigException("Unsupported RDF format");
		}

		try {
			RDFParser parser = Rio.createParser(format);

			Model model = new ModelImpl();
			parser.setRDFHandler(new StatementCollector(model));

			InputStream stream = new FileInputStream(file);
			try {
				parser.parse(stream, file.toURI().toString());
				return model;
			}
			catch (RDFParseException e) {
				throw new StoreConfigException(e);
			}
			catch (RDFHandlerException e) {
				throw new AssertionError(e);
			}
			finally {
				stream.close();
			}
		}
		catch (UnsupportedRDFormatException e) {
			throw new StoreConfigException(e);
		}
		catch (IOException e) {
			throw new StoreConfigException(e);
		}
	}

	public void addConfig(String id, Model config)
		throws StoreConfigException
	{
		if (getRdfFiles().containsKey(id)) {
			throw new StoreConfigException("Repository config already exists");
		}
		File file = new File(baseDir, id + ".ttl");
		saveConfig(file, config);
	}

	public void updateConfig(String id, Model config)
		throws StoreConfigException
	{
		File file = getRdfFiles().get(id);
		if (file == null) {
			throw new StoreConfigException("Repository config does not exist");
		}
		saveConfig(file, config);
	}

	public void removeConfig(String repositoryID)
		throws StoreConfigException
	{
		File file = getRdfFiles().get(repositoryID);
		if (file == null) {
			throw new StoreConfigException("No such repository config");
		}
		if (!file.delete()) {
			throw new StoreConfigException("Could not remove config");
		}
	}

	private Map<String, File> getRdfFiles()
		throws StoreConfigException
	{
		Map<String, File> map = new HashMap<String, File>();
		for (File file : baseDir.listFiles()) {
			if (file.isFile()) {
				String name = file.getName();
				String id = name.substring(0, name.lastIndexOf('.'));
				map.put(id, file);
			}
		}
		return map;
	}

	private void saveConfig(File file, Model config)
		throws StoreConfigException
	{
		RDFFormat format = Rio.getWriterFormatForFileName(file.getName());
		if (format == null) {
			throw new StoreConfigException("Unsupported RDF format");
		}

		try {
			OutputStream out = new FileOutputStream(file);

			try {
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
