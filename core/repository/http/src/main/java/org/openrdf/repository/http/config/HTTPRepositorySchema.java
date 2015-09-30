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
package org.openrdf.repository.http.config;

import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.repository.http.HTTPRepository;

/**
 * Defines constants for the HTTPRepository schema which is used by
 * {@link HTTPRepositoryFactory}s to initialize {@link HTTPRepository}s.
 * 
 * @author Arjohn Kampman
 */
public class HTTPRepositorySchema {

	/** The HTTPRepository schema namespace (<tt>http://www.openrdf.org/config/repository/http#</tt>). */
	public static final String NAMESPACE = "http://www.openrdf.org/config/repository/http#";

	/** <tt>http://www.openrdf.org/config/repository/http#repositoryURL</tt> */
	public final static IRI REPOSITORYURL;

	/** <tt>http://www.openrdf.org/config/repository/http#username</tt> */
	public final static IRI USERNAME;

	/** <tt>http://www.openrdf.org/config/repository/http#password</tt> */
	public final static IRI PASSWORD;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();
		REPOSITORYURL = factory.createIRI(NAMESPACE, "repositoryURL");
		USERNAME = factory.createIRI(NAMESPACE, "username");
		PASSWORD = factory.createIRI(NAMESPACE, "password");
	}
}
