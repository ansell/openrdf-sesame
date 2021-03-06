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
package org.openrdf.query.resultio;

import info.aduna.lang.service.FileFormatServiceRegistry;

/**
 * A registry that keeps track of the available
 * {@link BooleanQueryResultWriterFactory}s.
 * 
 * @author Arjohn Kampman
 */
public class BooleanQueryResultWriterRegistry extends
		FileFormatServiceRegistry<QueryResultFormat, BooleanQueryResultWriterFactory>
{

	/**
	 * Internal helper class to avoid continuous synchronized checking.
	 */
	private static class BooleanQueryResultWriterRegistryHolder {

		public static final BooleanQueryResultWriterRegistry instance = new BooleanQueryResultWriterRegistry();
	}

	/**
	 * Gets the default BooleanQueryResultWriterRegistry.
	 * 
	 * @return The default registry.
	 */
	public static BooleanQueryResultWriterRegistry getInstance() {
		return BooleanQueryResultWriterRegistryHolder.instance;
	}

	public BooleanQueryResultWriterRegistry() {
		super(BooleanQueryResultWriterFactory.class);
	}

	@Override
	protected QueryResultFormat getKey(BooleanQueryResultWriterFactory factory) {
		return factory.getBooleanQueryResultFormat();
	}
}
