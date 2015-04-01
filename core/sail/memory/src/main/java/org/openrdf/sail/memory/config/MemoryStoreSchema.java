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
package org.openrdf.sail.memory.config;

import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.sail.memory.MemoryStore;

/**
 * Defines constants for the MemoryStore schema which is used by
 * {@link MemoryStoreFactory}s to initialize {@link MemoryStore}s.
 * 
 * @author Arjohn Kampman
 */
public class MemoryStoreSchema {

	/** The MemoryStore schema namespace (<tt>http://www.openrdf.org/config/sail/memory#</tt>). */
	public static final String NAMESPACE = "http://www.openrdf.org/config/sail/memory#";

	/** <tt>http://www.openrdf.org/config/sail/memory#persist</tt> */
	public final static IRI PERSIST;

	/** <tt>http://www.openrdf.org/config/sail/memory#syncDelay</tt> */
	public final static IRI SYNC_DELAY;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		PERSIST = factory.createIRI(NAMESPACE, "persist");
		SYNC_DELAY = factory.createIRI(NAMESPACE, "syncDelay");
	}
}
