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
package org.openrdf.sail.lucene.config;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.sail.lucene.LuceneSail;

/**
 * Defines constants for the LuceneSail schema which is used by
 * {@link LuceneSailFactory}s to initialize {@link LuceneSail}s.
 */
public class LuceneSailConfigSchema {
	
	/** The LuceneSail schema namespace (<tt>http://www.openrdf.org/config/sail/lucene#</tt>). */
	public static final String NAMESPACE = "http://www.openrdf.org/config/sail/lucene#";

	public static final URI INDEX_DIR;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		INDEX_DIR = factory.createURI(NAMESPACE, "indexDir");
	}
}
