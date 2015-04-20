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
