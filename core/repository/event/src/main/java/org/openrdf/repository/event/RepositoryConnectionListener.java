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
package org.openrdf.repository.event;

import org.openrdf.model.Resource;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.repository.RepositoryConnection;

/**
 * Listener interface for connection modification.
 * 
 * @author James Leigh
 */
public interface RepositoryConnectionListener {

	public abstract void close(RepositoryConnection conn);

	/**
	 * @deprecated since release 2.7.0. Use {@link #begin(RepositoryConnection)} instead.
	 *  
	 * @param conn
	 * @param autoCommit
	 */
	@Deprecated
	public abstract void setAutoCommit(RepositoryConnection conn, boolean autoCommit);

	public abstract void begin(RepositoryConnection conn);
	
	public abstract void commit(RepositoryConnection conn);

	public abstract void rollback(RepositoryConnection conn);

	public abstract void add(RepositoryConnection conn, Resource subject, IRI predicate, Value object,
			Resource... contexts);

	public abstract void remove(RepositoryConnection conn, Resource subject, IRI predicate, Value object,
			Resource... contexts);

	public abstract void clear(RepositoryConnection conn, Resource... contexts);

	public abstract void setNamespace(RepositoryConnection conn, String prefix, String name);

	public abstract void removeNamespace(RepositoryConnection conn, String prefix);

	public abstract void clearNamespaces(RepositoryConnection conn);

	public abstract void execute(RepositoryConnection conn, QueryLanguage ql, String update,
			String baseURI, Update operation);
}
