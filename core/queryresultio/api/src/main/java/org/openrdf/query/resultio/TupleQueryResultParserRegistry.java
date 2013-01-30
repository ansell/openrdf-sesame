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
 * {@link TupleQueryResultParserFactory}s.
 * 
 * @author Arjohn Kampman
 */
public class TupleQueryResultParserRegistry extends
		FileFormatServiceRegistry<TupleQueryResultFormat, TupleQueryResultParserFactory>
{

	private static TupleQueryResultParserRegistry defaultRegistry;

	/**
	 * Gets the default TupleQueryResultParserRegistry.
	 * 
	 * @return The default registry.
	 */
	public static synchronized TupleQueryResultParserRegistry getInstance() {
		if (defaultRegistry == null) {
			defaultRegistry = new TupleQueryResultParserRegistry();
		}

		return defaultRegistry;
	}

	public TupleQueryResultParserRegistry() {
		super(TupleQueryResultParserFactory.class);
	}

	@Override
	protected TupleQueryResultFormat getKey(TupleQueryResultParserFactory factory) {
		return factory.getTupleQueryResultFormat();
	}
}
