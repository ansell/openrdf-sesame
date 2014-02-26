/*
 * Copyright Aduna, DFKI and L3S (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.lucene.config;

import org.openrdf.sail.Sail;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailFactory;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.lucene.LuceneSail;

/**
 * A {@link SailFactory} that creates {@link LuceneSail}s based on RDF
 * configuration data.
 */
public class LuceneSailFactory implements SailFactory {

	/**
	 * The type of repositories that are created by this factory.
	 * 
	 * @see SailFactory#getSailType()
	 */
	public static final String SAIL_TYPE = "openrdf:LuceneSail";

	/**
	 * Returns the Sail's type: <tt>openrdf:LuceneSail</tt>.
	 */
	public String getSailType() {
		return SAIL_TYPE;
	}

	public SailImplConfig getConfig() {
		return new LuceneSailConfig();
	}

	public Sail getSail(SailImplConfig config)
		throws SailConfigException
	{
		if (!SAIL_TYPE.equals(config.getType())) {
			throw new SailConfigException("Invalid Sail type: " + config.getType());
		}

		LuceneSail luceneSail = new LuceneSail();

		if (config instanceof LuceneSailConfig) {
			LuceneSailConfig luceneConfig = (LuceneSailConfig)config;
			luceneSail.setParameter(LuceneSail.LUCENE_DIR_KEY, luceneConfig.getIndexDir());
		}

		return luceneSail;
	}
}
