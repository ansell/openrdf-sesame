/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.transaction.operations;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.store.StoreException;

/**
 * Operation to remove statements matching specific pattern of subject,
 * predicate and object.
 * 
 * @author Arjohn Kampman
 * @author Leo Sauermann
 */
public class RemoveStatementsOperation extends StatementOperation {

	/**
	 * Creates a RemoveStatementsOperation.
	 */
	public RemoveStatementsOperation(Resource subj, URI pred, Value obj, Resource... contexts) {
		super(contexts);

		setSubject(subj);
		setPredicate(pred);
		setObject(obj);
	}

	public void execute(RepositoryConnection con)
		throws StoreException
	{
		con.removeMatch(getSubject(), getPredicate(), getObject(), getContexts());
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof RemoveStatementsOperation) {
			return super.equals(other);
		}

		return false;
	}
}
