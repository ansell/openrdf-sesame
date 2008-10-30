/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms;

import java.net.URL;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;
import org.openrdf.sail.helpers.SailMetaDataImpl;
import org.openrdf.sail.rdbms.config.RdbmsStoreFactory;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
class RdbmsStoreMetaData extends SailMetaDataImpl {

	private RdbmsStore store;

	private Logger logger = LoggerFactory.getLogger(RdbmsStoreMetaData.class);

	RdbmsStoreMetaData(RdbmsStore store) {
		this.store = store;
	}

	@Override
	public String getStoreName() {
		return RdbmsStoreFactory.SAIL_TYPE;
	}

	@Override
	public URL getLocation() {
		try {
			return store.getLocation();
		}
		catch (StoreException e) {
			logger.error(e.toString(), e);
			return null;
		}
	}

	@Override
	public boolean isReadOnly() {
		try {
			return !store.isWritable();
		}
		catch (StoreException e) {
			logger.error(e.toString(), e);
			return true;
		}
	}

	@Override
	public boolean isLocalStore() {
		return false;
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
