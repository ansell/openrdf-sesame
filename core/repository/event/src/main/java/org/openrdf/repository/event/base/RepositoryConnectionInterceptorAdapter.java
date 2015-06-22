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
package org.openrdf.repository.event.base;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.event.RepositoryConnectionInterceptor;

/**
 * @author Herko ter Horst
 */
public class RepositoryConnectionInterceptorAdapter implements RepositoryConnectionInterceptor {

	public boolean add(RepositoryConnection conn, Resource subject, URI predicate, Value object,
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

	public boolean remove(RepositoryConnection conn, Resource subject, URI predicate, Value object,
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
