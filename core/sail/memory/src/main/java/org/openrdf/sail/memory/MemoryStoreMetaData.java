/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;
import org.openrdf.sail.helpers.SailMetaDataImpl;
import org.openrdf.sail.memory.config.MemoryStoreFactory;
import org.openrdf.store.Isolation;

/**
 * @author James Leigh
 */
class MemoryStoreMetaData extends SailMetaDataImpl {

	private MemoryStore store;

	private Logger logger = LoggerFactory.getLogger(MemoryStoreMetaData.class);

	MemoryStoreMetaData(MemoryStore store) {
		super();
		this.store = store;
	}

	@Override
	public String getStoreName() {
		return MemoryStoreFactory.SAIL_TYPE;
	}

	@Override
	public URL getLocation() {
		File dataDir = store.getDataDir();
		if (dataDir == null) {
			return null;
		}
		try {
			return dataDir.toURI().toURL();
		}
		catch (MalformedURLException e) {
			logger.warn(e.toString(), e);
			return null;
		}
	}

	@Override
	public boolean isReadOnly() {
		return !store.isWritable();
	}

	@Override
	public Isolation getDefaultIsolation() {
		return Isolation.SERIALIZABLE;
	}

	@Override
	public String[] getQueryFunctions() {
		Set<String> functions = FunctionRegistry.getInstance().getKeys();
		return functions.toArray(new String[functions.size()]);
	}

	@Override
	public int getStoreMajorVersion() {
		return getSesameMajorVersion();
	}

	@Override
	public int getStoreMinorVersion() {
		return getSesameMinorVersion();
	}

	@Override
	public String getStoreVersion() {
		return getSesameVersion();
	}

	@Override
	public boolean isContextBNodesSupported() {
		return true;
	}

	@Override
	public boolean isContextSupported() {
		return true;
	}

	@Override
	public boolean isEmbedded() {
		return true;
	}

	@Override
	public boolean isMatchingOnlySameTerm() {
		return true;
	}

	@Override
	public boolean isLiteralDatatypePreserved() {
		return true;
	}

	@Override
	public boolean isLiteralLabelPreserved() {
		return true;
	}

}
