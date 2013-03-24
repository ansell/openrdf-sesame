/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.sail.config;

import org.openrdf.sail.Sail;

/**
 * A SailFactory takes care of creating and initializing a specific type of
 * {@link Sail} based on RDF configuration data. SailFactory's are used to
 * create specific Sails and to initialize them based on the configuration data
 * that is supplied to it, for example in a server environment.
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
	 * 
	 * @param config
	 *        TODO
	 * @return The created (but un-initialized) Sail.
	 * @throws SailConfigException
	 *         If no Sail could be created due to invalid or incomplete
	 *         configuration data.
	 */
	public Sail getSail(SailImplConfig config)
		throws SailConfigException;
}
