/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.config;

import org.openrdf.sail.Sail;

/**
 * A SailFactory takes care of creating and initializing a specific type of
 * {@link Sail}s based on RDF configuration data. SailFactory's are used by the
 * {@link org.openrdf.repository.sail.SailRepositoryFactory} to create specific
 * Sails and to initialize them based on the configuration data that iis
 * supplied to it, for example in a server environment.
 * 
 * @author Arjohn Kampman
 */
public interface SailFactory {

	/**
	 * Returns the type of the Sails that this factory creates. Sail types are
	 * used for identification and should uniquely identify specific
	 * implementations of the Sail API. This type <em>can</em> be equal to the
	 * fully qualified class name of the Sail, but this is not required.
	 */
	public String getSailType();

	public SailImplConfig getConfig();

	/**
	 * Returns a Sail instance that has been initialized using the supplied
	 * configuration data.
	 * @param config TODO
	 * 
	 * @return The created (but un-initialized) Sail.
	 * @throws SailConfigException
	 *         If no Sail could be created due to invalid or incomplete
	 *         configuration data.
	 */
	public Sail getSail(SailImplConfig config)
		throws SailConfigException;
}
