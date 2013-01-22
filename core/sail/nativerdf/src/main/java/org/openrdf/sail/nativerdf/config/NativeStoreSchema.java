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
package org.openrdf.sail.nativerdf.config;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.sail.nativerdf.NativeStore;

/**
 * Defines constants for the NativeStore schema which is used by
 * {@link NativeStoreFactory}s to initialize {@link NativeStore}s.
 * 
 * @author Arjohn Kampman
 */
public class NativeStoreSchema {

	/** The NativeStore schema namespace (<tt>http://www.openrdf.org/config/sail/native#</tt>). */
	public static final String NAMESPACE = "http://www.openrdf.org/config/sail/native#";

	/** <tt>http://www.openrdf.org/config/sail/native#tripleIndexes</tt> */
	public final static URI TRIPLE_INDEXES;

	/** <tt>http://www.openrdf.org/config/sail/native#forceSync</tt> */
	public final static URI FORCE_SYNC;

	/** <tt>http://www.openrdf.org/config/sail/native#valueCacheSize</tt> */
	public final static URI VALUE_CACHE_SIZE;

	/** <tt>http://www.openrdf.org/config/sail/native#valueIDCacheSize</tt> */
	public final static URI VALUE_ID_CACHE_SIZE;

	/** <tt>http://www.openrdf.org/config/sail/native#namespaceCacheSize</tt> */
	public final static URI NAMESPACE_CACHE_SIZE;

	/** <tt>http://www.openrdf.org/config/sail/native#namespaceIDCacheSize</tt> */
	public final static URI NAMESPACE_ID_CACHE_SIZE;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		TRIPLE_INDEXES = factory.createURI(NAMESPACE, "tripleIndexes");
		FORCE_SYNC = factory.createURI(NAMESPACE, "forceSync");
		VALUE_CACHE_SIZE = factory.createURI(NAMESPACE, "valueCacheSize");
		VALUE_ID_CACHE_SIZE = factory.createURI(NAMESPACE, "valueIDCacheSize");
		NAMESPACE_CACHE_SIZE = factory.createURI(NAMESPACE, "namespaceCacheSize");
		NAMESPACE_ID_CACHE_SIZE = factory.createURI(NAMESPACE, "namespaceIDCacheSize");
	}
}
