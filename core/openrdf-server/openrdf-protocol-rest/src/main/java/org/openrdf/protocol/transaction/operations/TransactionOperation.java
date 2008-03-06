/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.protocol.transaction.operations;

import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;


/**
 * used to represent operations of Transactions during the serialization and
 * parsing of HTTPSail. They have to implement a correct equals() method for
 * optimization and testing.
 * 
 */
public abstract class TransactionOperation {

	/**
	 * the name used in the XML serialization of this operation.
	 * 
	 * @return the name.
	 */
	public abstract String getOperationXMLElementName();

	/**
	 * execute this operation on the passed transaction. After serialization and
	 * deserialization, the operation is now executed on the server sail.
	 * 
	 * @param transaction
	 *            the transaction, on the server side, that this operation
	 *            should now work on.
	 * @throws SailException
	 *             if the transaction does not accept this
	 */
	public abstract void executeOperationOnTransaction(SailConnection transaction)
			throws SailException;

	@Override
	public String toString() {
		return getOperationXMLElementName();
	}
}
