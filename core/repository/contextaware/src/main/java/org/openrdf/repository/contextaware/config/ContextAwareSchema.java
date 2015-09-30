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
package org.openrdf.repository.contextaware.config;

import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 * @author James Leigh
 */
public class ContextAwareSchema {

	/**
	 * The ContextAwareRepository schema namespace (
	 * <tt>http://www.openrdf.org/config/repository/contextaware#</tt>).
	 */
	public static final String NAMESPACE = "http://www.openrdf.org/config/repository/contextaware#";

	/** <tt>http://www.openrdf.org/config/repository/contextaware#includeInferred</tt> */
	public final static IRI INCLUDE_INFERRED;

	/** <tt>http://www.openrdf.org/config/repository/contextaware#maxQueryTime</tt> */
	public final static IRI MAX_QUERY_TIME;

	/** <tt>http://www.openrdf.org/config/repository/contextaware#queryLanguage</tt> */
	public final static IRI QUERY_LANGUAGE;

	/** <tt>http://www.openrdf.org/config/repository/contextaware#base</tt> */
	public final static IRI BASE_URI;

	/** <tt>http://www.openrdf.org/config/repository/contextaware#readContext</tt> */
	public final static IRI READ_CONTEXT;

	/** <tt>http://www.openrdf.org/config/repository/contextaware#addContext</tt> */
	@Deprecated
	public final static IRI ADD_CONTEXT;

	/** <tt>http://www.openrdf.org/config/repository/contextaware#removeContext</tt> */
	public final static IRI REMOVE_CONTEXT;

	/** <tt>http://www.openrdf.org/config/repository/contextaware#archiveContext</tt> */
	@Deprecated
	public final static IRI ARCHIVE_CONTEXT;

	/** <tt>http://www.openrdf.org/config/repository/contextaware#insertContext</tt> */
	public final static IRI INSERT_CONTEXT;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();
		INCLUDE_INFERRED = factory.createIRI(NAMESPACE, "includeInferred");
		QUERY_LANGUAGE = factory.createIRI(NAMESPACE, "ql");
		BASE_URI = factory.createIRI(NAMESPACE, "base");
		READ_CONTEXT = factory.createIRI(NAMESPACE, "readContext");
		ADD_CONTEXT = factory.createIRI(NAMESPACE, "addContext");
		REMOVE_CONTEXT = factory.createIRI(NAMESPACE, "removeContext");
		ARCHIVE_CONTEXT = factory.createIRI(NAMESPACE, "archiveContext");
		INSERT_CONTEXT = factory.createIRI(NAMESPACE, "insertContext");
		MAX_QUERY_TIME = factory.createIRI(NAMESPACE, "maxQueryTime");
	}
}
