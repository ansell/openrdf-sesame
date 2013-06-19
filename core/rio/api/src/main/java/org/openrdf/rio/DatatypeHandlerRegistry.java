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
package org.openrdf.rio;

import info.aduna.lang.service.ServiceRegistry;

/**
 * Registry of {@link DatatypeHandler}s.
 * 
 * @author Peter Ansell
 */
public class DatatypeHandlerRegistry extends ServiceRegistry<String, DatatypeHandler> {

	private static DatatypeHandlerRegistry defaultRegistry;

	/**
	 * Gets the default QueryParserRegistry.
	 * 
	 * @return The default registry.
	 */
	public static synchronized DatatypeHandlerRegistry getInstance() {
		if (defaultRegistry == null) {
			defaultRegistry = new DatatypeHandlerRegistry();
		}

		return defaultRegistry;
	}

	public DatatypeHandlerRegistry() {
		super(DatatypeHandler.class);
	}

	@Override
	protected String getKey(DatatypeHandler handler) {
		return handler.getKey();
	}

}
