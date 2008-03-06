/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
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
 * Operation to remove statements matching specific pattern of subject,
 * predicate and object.
 * 
 * @author Arjohn Kampman
 */
public class RemoveStatementsOperation extends ContextStatementOperation {

	/**
	 * Creates an uninitialized RemoveStatementsOperation; subject, predicate,
	 * object and (optionally) contexts can be set afterwards.
	 */
	public RemoveStatementsOperation() {
	}

	public RemoveStatementsOperation(Resource subj, URI pred, Value obj, Resource... contexts) {
		this();

		setSubject(subj);
		setPredicate(pred);
		setObject(obj);
		setContexts(contexts);
	}

	public void execute(SailConnection con)
		throws SailException
	{
		con.removeStatements(getSubject(), getPredicate(), getObject(), getContexts());
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof RemoveStatementsOperation) {
			return super.equals((RemoveStatementsOperation)other);
		}

		return false;
	}
}
