/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.openrdf.sail.nativerdf.config;

import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
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
	public final static IRI TRIPLE_INDEXES;

	/** <tt>http://www.openrdf.org/config/sail/native#forceSync</tt> */
	public final static IRI FORCE_SYNC;

	/** <tt>http://www.openrdf.org/config/sail/native#valueCacheSize</tt> */
	public final static IRI VALUE_CACHE_SIZE;

	/** <tt>http://www.openrdf.org/config/sail/native#valueIDCacheSize</tt> */
	public final static IRI VALUE_ID_CACHE_SIZE;

	/** <tt>http://www.openrdf.org/config/sail/native#namespaceCacheSize</tt> */
	public final static IRI NAMESPACE_CACHE_SIZE;

	/** <tt>http://www.openrdf.org/config/sail/native#namespaceIDCacheSize</tt> */
	public final static IRI NAMESPACE_ID_CACHE_SIZE;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();
		TRIPLE_INDEXES = factory.createIRI(NAMESPACE, "tripleIndexes");
		FORCE_SYNC = factory.createIRI(NAMESPACE, "forceSync");
		VALUE_CACHE_SIZE = factory.createIRI(NAMESPACE, "valueCacheSize");
		VALUE_ID_CACHE_SIZE = factory.createIRI(NAMESPACE, "valueIDCacheSize");
		NAMESPACE_CACHE_SIZE = factory.createIRI(NAMESPACE, "namespaceCacheSize");
		NAMESPACE_ID_CACHE_SIZE = factory.createIRI(NAMESPACE, "namespaceIDCacheSize");
	}
}
