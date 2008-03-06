/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.protocol.transaction.operations;

import java.util.ArrayList;

import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * A list of operations.
 */
public class OperationList extends ArrayList<TransactionOperation> {

	/**
	 * 
	 */
	public OperationList() {
		super();
	}

	/**
	 * execute all operations on the passed transaction. After serialization and
	 * deserialization, the operations are now executed on the server sail.
	 * 
	 * @param transaction
	 *            the transaction, on the server side, that this operation
	 *            should now work on.
	 * @throws SailException
	 *             if the transaction does not accept this
	 */
	public void executeOperationsOnTransaction(SailConnection transaction)
			throws SailException {
		for (TransactionOperation op : this) {
			op.executeOperationOnTransaction(transaction);
		}
	}

	/**
	 * return true, if this operationlist contains the same operations as the
	 * passed list. the passed list can be optimized, meaning that multiple
	 * operations that are equal() and behind each other can be reduced to one.
	 * 
	 * @param optimized
	 *            the list to compare with. This list can be non-optimized (=it
	 *            can contain more than the optimized)
	 * @return true, if they are the same.
	 */
	public boolean equalsOptimizedVersion(OperationList optimized) {
		if ((this.size() > 0) && (optimized.size() == 0))
			return false;
		if ((optimized.size() == 0) && (this.size() > 0))
			return false;
		int otherindex = 0;
		int thisindex = 0;
		while (thisindex < this.size()) {
			TransactionOperation op = this.get(thisindex);
			if (otherindex >= optimized.size())
				return false;
			TransactionOperation other = optimized.get(otherindex);
			if (!op.equals(other))
				return false;
			// skip anything that is the same as the current op on this
			thisindex++;
			while ((thisindex < size() && get(thisindex).equals(op))) {
				thisindex++;
			}
			// skip anything that is the same as the current op on the other
			// index
			otherindex++;
			while ((otherindex < optimized.size() && optimized.get(otherindex)
					.equals(op))) {
				otherindex++;
			}
		}
		return true;
	}

}
