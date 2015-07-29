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
 * {@link TupleQueryResultWriterFactory}s.
 * 
 * @author Arjohn Kampman
 */
public class TupleQueryResultWriterRegistry extends
		FileFormatServiceRegistry<QueryResultFormat, TupleQueryResultWriterFactory>
{

	private static TupleQueryResultWriterRegistry defaultRegistry;

	/**
	 * Gets the default TupleQueryResultWriterRegistry.
	 * 
	 * @return The default registry.
	 */
	public static synchronized TupleQueryResultWriterRegistry getInstance() {
		if (defaultRegistry == null) {
			defaultRegistry = new TupleQueryResultWriterRegistry();
		}

		return defaultRegistry;
	}

	public TupleQueryResultWriterRegistry() {
		super(TupleQueryResultWriterFactory.class);
	}

	@Override
	protected QueryResultFormat getKey(TupleQueryResultWriterFactory factory) {
		return factory.getTupleQueryResultFormat();
	}
}
