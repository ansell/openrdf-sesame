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
package org.eclipse.rdf4j.repository.config;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Defines constants for the repository configuration schema that is used by
 * {@link org.eclipse.rdf4j.repository.manager.RepositoryManager}s.
 * 
 * @author Arjohn Kampman
 */
public class RepositoryConfigSchema {

	/** The HTTPRepository schema namespace (<tt>http://www.openrdf.org/config/repository#</tt>). */
	public static final String NAMESPACE = "http://www.openrdf.org/config/repository#";

	/** <tt>http://www.openrdf.org/config/repository#RepositoryContext</tt> */
	public final static IRI REPOSITORY_CONTEXT;

	/** <tt>http://www.openrdf.org/config/repository#Repository</tt> */
	public final static IRI REPOSITORY;

	/** <tt>http://www.openrdf.org/config/repository#repositoryID</tt> */
	public final static IRI REPOSITORYID;

	/** <tt>http://www.openrdf.org/config/repository#repositoryImpl</tt> */
	public final static IRI REPOSITORYIMPL;

	/** <tt>http://www.openrdf.org/config/repository#repositoryType</tt> */
	public final static IRI REPOSITORYTYPE;

	/** <tt>http://www.openrdf.org/config/repository#delegate</tt> */
	public final static IRI DELEGATE;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();
		REPOSITORY_CONTEXT = factory.createIRI(NAMESPACE, "RepositoryContext");
		REPOSITORY = factory.createIRI(NAMESPACE, "Repository");
		REPOSITORYID = factory.createIRI(NAMESPACE, "repositoryID");
		REPOSITORYIMPL = factory.createIRI(NAMESPACE, "repositoryImpl");
		REPOSITORYTYPE = factory.createIRI(NAMESPACE, "repositoryType");
		DELEGATE = factory.createIRI(NAMESPACE, "delegate");
	}
}
