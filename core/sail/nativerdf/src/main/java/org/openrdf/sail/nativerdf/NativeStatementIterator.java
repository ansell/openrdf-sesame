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
package org.openrdf.sail.nativerdf;

import java.io.IOException;

import info.aduna.io.ByteArrayUtil;
import info.aduna.iteration.LookAheadIteration;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.SailException;
import org.openrdf.sail.derived.RdfIteration;
import org.openrdf.sail.nativerdf.btree.RecordIterator;

/**
 * A statement iterator that wraps a RecordIterator containing statement records
 * and translates these records to {@link Statement} objects.
 */
class NativeStatementIterator extends LookAheadIteration<Statement, SailException> implements
		RdfIteration<Statement>
{

	/*-----------*
	 * Variables *
	 *-----------*/

	private final RecordIterator btreeIter;

	private final ValueStore valueStore;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new NativeStatementIterator.
	 */
	public NativeStatementIterator(RecordIterator btreeIter, ValueStore valueStore)
		throws IOException
	{
		this.btreeIter = btreeIter;
		this.valueStore = valueStore;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Statement getNextElement()
		throws SailException
	{
		try {
			byte[] nextValue = btreeIter.next();

			if (nextValue == null) {
				return null;
			}

			int subjID = ByteArrayUtil.getInt(nextValue, TripleStore.SUBJ_IDX);
			Resource subj = (Resource)valueStore.getValue(subjID);

			int predID = ByteArrayUtil.getInt(nextValue, TripleStore.PRED_IDX);
			URI pred = (URI)valueStore.getValue(predID);

			int objID = ByteArrayUtil.getInt(nextValue, TripleStore.OBJ_IDX);
			Value obj = valueStore.getValue(objID);

			Resource context = null;
			int contextID = ByteArrayUtil.getInt(nextValue, TripleStore.CONTEXT_IDX);
			if (contextID != 0) {
				context = (Resource)valueStore.getValue(contextID);
			}

			return valueStore.createStatement(subj, pred, obj, context);
		}
		catch (IOException e) {
			throw causeIOException(e);
		}
	}

	@Override
	protected void handleClose()
		throws SailException
	{
		super.handleClose();
		try {
			btreeIter.close();
		}
		catch (IOException e) {
			throw causeIOException(e);
		}
	}

	protected SailException causeIOException(IOException e) {
		return new SailException(e);
	}
}
