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

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Defines constants for the Sail repository schema which are used to initialize
 * repositories.
 * 
 * @author Arjohn Kampman
 */
public class SailConfigSchema {

	/**
	 * The Sail API schema namespace (
	 * <tt>http://www.openrdf.org/config/sail#</tt>).
	 */
	public static final String NAMESPACE = "http://www.openrdf.org/config/sail#";

	/** <tt>http://www.openrdf.org/config/sail#sailType</tt> */
	public final static URI SAILTYPE;

	/** <tt>http://www.openrdf.org/config/sail#delegate</tt> */
	public final static URI DELEGATE;
	
	/** <tt>http://www.openrdf.org/config/sail#iterationCacheSyncTreshold</tt> */
	public final static URI ITERATION_CACHE_SYNC_THRESHOLD;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		SAILTYPE = factory.createURI(NAMESPACE, "sailType");
		DELEGATE = factory.createURI(NAMESPACE, "delegate");
		ITERATION_CACHE_SYNC_THRESHOLD = factory.createURI(NAMESPACE, "iterationCacheSyncTreshold");
	}
}
