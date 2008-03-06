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
 * predicate, object and context from named contexts.
 * 
 * @author Arjohn Kampman
 */
public class RemoveNamedContextStatementsOperation extends ContextStatementOperation {

	/**
	 * Creates an uninitialized RemoveNamedContextStatementsOperation; subject, predicate,
	 * object and context can be set afterwards.
	 */
	public RemoveNamedContextStatementsOperation() {
	}

	public RemoveNamedContextStatementsOperation(Resource subj, URI pred, Value obj, Resource context) {
		this();

		setSubject(subj);
		setPredicate(pred);
		setObject(obj);
		setContext(context);
	}

	public void execute(SailConnection con)
		throws SailException
	{
		con.removeStatements(getSubject(), getPredicate(), getObject(), getContext());
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof RemoveNamedContextStatementsOperation) {
			return super.equals((RemoveNamedContextStatementsOperation)other);
		}

		return false;
	}
}
