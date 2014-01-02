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
package org.openrdf.sail.federation;

import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.SailException;

/**
 * Echos all write operations to all members.
 * 
 * @author James Leigh
 */
abstract class AbstractEchoWriteConnection extends AbstractFederationConnection {

	public AbstractEchoWriteConnection(Federation federation, List<RepositoryConnection> members) {
		super(federation, members);
	}

	@Override
	public void startTransactionInternal()
		throws SailException
	{
		excute(new Procedure() {

			public void run(RepositoryConnection con)
				throws RepositoryException
			{
				con.begin();
			}
		});
	}

	@Override
	public void rollbackInternal()
		throws SailException
	{
		excute(new Procedure() {

			public void run(RepositoryConnection con)
				throws RepositoryException
			{
				con.rollback();
			}
		});
	}

	@Override
	public void commitInternal()
		throws SailException
	{
		excute(new Procedure() {

			public void run(RepositoryConnection con)
				throws RepositoryException
			{
				con.commit();
			}
		});
	}

	public void setNamespaceInternal(final String prefix, final String name)
		throws SailException
	{
		excute(new Procedure() {

			public void run(RepositoryConnection con)
				throws RepositoryException
			{
				con.setNamespace(prefix, name);
			}
		});
	}

	@Override
	public void clearNamespacesInternal()
		throws SailException
	{
		excute(new Procedure() {

			public void run(RepositoryConnection con)
				throws RepositoryException
			{
				con.clearNamespaces();
			}
		});
	}

	@Override
	public void removeNamespaceInternal(final String prefix)
		throws SailException
	{
		excute(new Procedure() {

			public void run(RepositoryConnection con)
				throws RepositoryException
			{
				con.removeNamespace(prefix);
			}
		});
	}

	@Override
	public void removeStatementsInternal(final Resource subj, final URI pred, final Value obj,
			final Resource... contexts)
		throws SailException
	{
		excute(new Procedure() {

			public void run(RepositoryConnection con)
				throws RepositoryException
			{
				con.remove(subj, pred, obj, contexts);
			}
		});
	}
}
