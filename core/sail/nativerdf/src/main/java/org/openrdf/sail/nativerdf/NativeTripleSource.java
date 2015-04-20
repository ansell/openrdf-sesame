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
import java.nio.channels.ClosedByInterruptException;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ExceptionConvertingIteration;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.algebra.evaluation.TripleSource;

public class NativeTripleSource implements TripleSource {

	/*-----------*
	 * Constants *
	 *-----------*/

	protected final NativeStore nativeStore;

	protected final boolean includeInferred;

	protected final boolean readTransaction;

	/*--------------*
	 * Constructors *
	 *--------------*/

	protected NativeTripleSource(NativeStore store, boolean includeInferred, boolean readTransaction) {
		this.nativeStore = store;
		this.includeInferred = includeInferred;
		this.readTransaction = readTransaction;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public CloseableIteration<? extends Statement, QueryEvaluationException> getStatements(Resource subj,
			IRI pred, Value obj, Resource... contexts)
		throws QueryEvaluationException
	{
		try {
			return new ExceptionConvertingIteration<Statement, QueryEvaluationException>(
					nativeStore.createStatementIterator(subj, pred, obj, includeInferred, readTransaction,
							contexts))
			{

				@Override
				protected QueryEvaluationException convert(Exception e) {
					if (e instanceof ClosedByInterruptException) {
						return new QueryInterruptedException(e);
					}
					else if (e instanceof IOException) {
						return new QueryEvaluationException(e);
					}
					else if (e instanceof RuntimeException) {
						throw (RuntimeException)e;
					}
					else if (e == null) {
						throw new IllegalArgumentException("e must not be null");
					}
					else {
						throw new IllegalArgumentException("Unexpected exception type: " + e.getClass());
					}
				}
			};
		}
		catch (IOException e) {
			throw new QueryEvaluationException("Unable to get statements", e);
		}
	}

	public ValueFactory getValueFactory() {
		return nativeStore.getValueFactory();
	}
}
