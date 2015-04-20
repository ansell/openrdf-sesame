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
package org.openrdf.http.protocol.transaction.operations;

import org.openrdf.model.Resource;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Operation to add a statement.
 * 
 * @author Arjohn Kampman
 * @author Leo Sauermann
 */
public class AddStatementOperation extends StatementOperation {

	/**
	 * Create an AddStatementOperation.
	 */
	public AddStatementOperation(Resource subj, IRI pred, Value obj, Resource... contexts) {
		super(contexts);

		assert subj != null : "subj must not be null";
		assert pred != null : "pred must not be null";
		assert obj != null : "obj must not be null";

		setSubject(subj);
		setPredicate(pred);
		setObject(obj);
	}

	public void execute(RepositoryConnection con)
		throws RepositoryException
	{
		con.add(getSubject(), getPredicate(), getObject(), getContexts());
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof AddStatementOperation) {
			return super.equals(other);
		}

		return false;
	}
}
