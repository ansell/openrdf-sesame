/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.dataset.config;

import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.sail.Sail;
import org.openrdf.sail.config.SailFactory;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.dataset.DatasetSail;
import org.openrdf.store.StoreConfigException;

/**
 * A {@link RepositoryFactory} that creates {@link DatasetSail}s based on
 * RDF configuration data.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class DatasetFactory implements SailFactory {

	/**
	 * The type of sails that are created by this factory.
	 * 
	 * @see SailFactory#getSailType()
	 */
	public static final String SAIL_TYPE = "openrdf:DatasetSail";

	/**
	 * Returns the sail's type: <tt>openrdf:DatasetSail</tt>.
	 */
	public String getSailType() {
		return SAIL_TYPE;
	}

	public SailImplConfig getConfig() {
		return new DatasetConfig();
	}

	public Sail getSail(SailImplConfig config)
		throws StoreConfigException
	{
		if (config instanceof DatasetConfig) {
			DatasetConfig dc = (DatasetConfig) config;
			DatasetSail sail = new DatasetSail();
			sail.setNamedGraphs(dc.getNamedGraphs());
			sail.setClosed(dc.isClosed());
			return sail;
		}

		throw new StoreConfigException("Invalid configuration class: " + config.getClass());
	}
}
