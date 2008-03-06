/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.transaction.operations;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * Operation to add a statement.
 * 
 * @author Arjohn Kampman
 */
public class AddStatementOperation extends StatementOperation {

	/**
	 * Create an AddStatementOperation.
	 */
	public AddStatementOperation(Resource subj, URI pred, Value obj, Resource... contexts) {
		super(contexts);

		assert subj != null : "subj must not be null";
		assert pred != null : "pred must not be null";
		assert obj != null : "obj must not be null";

		setSubject(subj);
		setPredicate(pred);
		setObject(obj);
	}

	public void execute(SailConnection con)
		throws SailException
	{
		con.addStatement(getSubject(), getPredicate(), getObject(), getContexts());
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
