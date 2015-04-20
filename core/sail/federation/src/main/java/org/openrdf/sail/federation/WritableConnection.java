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
import java.util.Random;

import org.openrdf.model.Resource;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.SailException;

/**
 * Statements are only written to a single member. Statements that have a
 * {@link IllegalStatementException} throw when added to a member are tried
 * against all other members until it is accepted. If no members accept a
 * statement the original exception is re-thrown.
 * 
 * @author James Leigh
 */
class WritableConnection extends AbstractEchoWriteConnection {

	private int addIndex;

	public WritableConnection(Federation federation, List<RepositoryConnection> members)
		throws SailException
	{
		super(federation, members);
		int size = members.size();
		int rnd = (new Random().nextInt() % size + size) % size;
		// use round-robin to distribute the load
		for (int i = rnd, n = rnd + size; i < n; i++) {
			try {
				if (members.get(i % size).getRepository().isWritable()) {
					addIndex = i % size;
				}
			}
			catch (RepositoryException e) {
				throw new SailException(e);
			}
		}
	}

	@Override
	public void addStatementInternal(Resource subj, IRI pred, Value obj, Resource... contexts)
		throws SailException
	{
		add(members.get(addIndex), subj, pred, obj, contexts);
	}

	private void add(RepositoryConnection member, Resource subj, IRI pred, Value obj, Resource... contexts)
		throws SailException
	{
		try {
			member.add(subj, pred, obj, contexts);
		}
		catch (RepositoryException e) {
			throw new SailException(e);
		}
	}

	@Override
	protected void clearInternal(Resource... contexts)
		throws SailException
	{
		removeStatementsInternal(null, null, null, contexts);
	}

}
