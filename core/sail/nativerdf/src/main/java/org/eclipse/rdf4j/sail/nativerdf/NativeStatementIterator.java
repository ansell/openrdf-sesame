/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.nativerdf;

import java.io.IOException;

import org.eclipse.rdf4j.common.io.ByteArrayUtil;
import org.eclipse.rdf4j.common.iteration.LookAheadIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.nativerdf.btree.RecordIterator;

/**
 * A statement iterator that wraps a RecordIterator containing statement records
 * and translates these records to {@link Statement} objects.
 */
class NativeStatementIterator extends LookAheadIteration<Statement, SailException> {

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
		IRI pred = (IRI)valueStore.getValue(predID);

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
