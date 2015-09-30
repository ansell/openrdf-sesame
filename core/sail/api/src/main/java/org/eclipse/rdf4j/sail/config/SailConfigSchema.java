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
package org.eclipse.rdf4j.sail.config;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

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
	public final static IRI SAILTYPE;

	/** <tt>http://www.openrdf.org/config/sail#delegate</tt> */
	public final static IRI DELEGATE;
	
	/** <tt>http://www.openrdf.org/config/sail#iterationCacheSyncTreshold</tt> */
	public final static IRI ITERATION_CACHE_SYNC_THRESHOLD;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();
		SAILTYPE = factory.createIRI(NAMESPACE, "sailType");
		DELEGATE = factory.createIRI(NAMESPACE, "delegate");
		ITERATION_CACHE_SYNC_THRESHOLD = factory.createIRI(NAMESPACE, "iterationCacheSyncTreshold");
	}
}
