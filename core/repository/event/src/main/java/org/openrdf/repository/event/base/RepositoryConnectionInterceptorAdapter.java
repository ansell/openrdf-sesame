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
package org.openrdf.repository.event.base;

import org.openrdf.model.Resource;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.event.RepositoryConnectionInterceptor;

/**
 * @author Herko ter Horst
 */
public class RepositoryConnectionInterceptorAdapter implements RepositoryConnectionInterceptor {

	public boolean add(RepositoryConnection conn, Resource subject, IRI predicate, Value object,
			Resource... contexts)
	{
		return false;
	}

	public boolean begin(RepositoryConnection conn) {
		return false;
	}
	
	public boolean clear(RepositoryConnection conn, Resource... contexts) {
		return false;
	}

	public boolean clearNamespaces(RepositoryConnection conn) {
		return false;
	}

	public boolean close(RepositoryConnection conn) {
		return false;
	}

	public boolean commit(RepositoryConnection conn) {
		return false;
	}

	public boolean remove(RepositoryConnection conn, Resource subject, IRI predicate, Value object,
			Resource... contexts)
	{
		return false;
	}

	public boolean removeNamespace(RepositoryConnection conn, String prefix) {
		return false;
	}

	public boolean rollback(RepositoryConnection conn) {
		return false;
	}

	@Deprecated
	public boolean setAutoCommit(RepositoryConnection conn, boolean autoCommit) {
		return false;
	}

	public boolean setNamespace(RepositoryConnection conn, String prefix, String name) {
		return false;
	}

	public boolean execute(RepositoryConnection conn, QueryLanguage ql, String update, String baseURI,
			Update operation)
	{
		return false;
	}


}
