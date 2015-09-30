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
package org.eclipse.rdf4j.repository.event;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;

/**
 * Interceptor interface for connection modification. 
 * 
 * @author Herko ter Horst
 */
public interface RepositoryConnectionInterceptor {

	/**
	 * @param conn
	 *        the RepositoryConnection to perform the operation on.
	 * @return true if the interceptor has been denied access to the operation, false
	 *         otherwise.
	 */
	public abstract boolean close(RepositoryConnection conn);

	/**
	 * @param conn
	 *        the RepositoryConnection to perform the operation on.
	 * @return true if the interceptor has been denied access to the operation, false
	 *         otherwise.
	 */
	public abstract boolean begin(RepositoryConnection conn);

	/**
	 * @deprecated since 2.7.0. Use {@link #begin(RepositoryConnection)} instead.
	 * @param conn
	 *        the RepositoryConnection to perform the operation on.
	 * @param autoCommit
	 * @return true if the interceptor has been denied access to the operation, false
	 *         otherwise.
	 */
	@Deprecated
	public abstract boolean setAutoCommit(RepositoryConnection conn, boolean autoCommit);

	/**
	 * @param conn
	 *        the RepositoryConnection to perform the operation on.
	 * @return true if the interceptor has been denied access to the operation, false
	 *         otherwise.
	 */
	public abstract boolean commit(RepositoryConnection conn);

	/**
	 * @param conn
	 *        the RepositoryConnection to perform the operation on.
	 * @return true if the interceptor has been denied access to the operation, false
	 *         otherwise.
	 */
	public abstract boolean rollback(RepositoryConnection conn);

	/**
	 * @param conn
	 *        the RepositoryConnection to perform the operation on.
	 * @return true if the interceptor has been denied access to the operation, false
	 *         otherwise.
	 */
	public abstract boolean add(RepositoryConnection conn, Resource subject, IRI predicate, Value object,
			Resource... contexts);

	/**
	 * @param conn
	 *        the RepositoryConnection to perform the operation on.
	 * @return true if the interceptor has been denied access to the operation, false
	 *         otherwise.
	 */
	public abstract boolean remove(RepositoryConnection conn, Resource subject, IRI predicate, Value object,
			Resource... contexts);

	/**
	 * @param conn
	 *        the RepositoryConnection to perform the operation on.
	 * @return true if the interceptor has been denied access to the operation, false
	 *         otherwise.
	 */
	public abstract boolean clear(RepositoryConnection conn, Resource... contexts);

	/**
	 * @param conn
	 *        the RepositoryConnection to perform the operation on.
	 * @return true if the interceptor has been denied access to the operation, false
	 *         otherwise.
	 */
	public abstract boolean setNamespace(RepositoryConnection conn, String prefix, String name);

	/**
	 * @param conn
	 *        the RepositoryConnection to perform the operation on.
	 * @return true if the interceptor has been denied access to the operation, false
	 *         otherwise.
	 */
	public abstract boolean removeNamespace(RepositoryConnection conn, String prefix);

	/**
	 * @param conn
	 *        the RepositoryConnection to perform the operation on.
	 * @return true if the interceptor has been denied access to the operation, false
	 *         otherwise.
	 */
	public abstract boolean clearNamespaces(RepositoryConnection conn);

	/**
	 * @param conn
	 *        the RepositoryConnection to perform the operation on.
	 * @return true if the interceptor has been denied access to the operation, false
	 *         otherwise.
	 */
	public abstract boolean execute(RepositoryConnection conn, QueryLanguage ql, String update,
			String baseURI, Update operation);
}
