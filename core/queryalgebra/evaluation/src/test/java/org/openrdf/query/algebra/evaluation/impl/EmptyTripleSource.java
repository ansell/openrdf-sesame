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
package org.openrdf.query.algebra.evaluation.impl;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;

import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.TripleSource;

public class EmptyTripleSource implements TripleSource {

	private final ValueFactory vf;

	public EmptyTripleSource() {
		this(SimpleValueFactory.getInstance());
	}

	public EmptyTripleSource(ValueFactory vf) {
		this.vf = vf;
	}

	@Override
	public ValueFactory getValueFactory() {
		return vf;
	}

	@Override
	public CloseableIteration<? extends Statement, QueryEvaluationException> getStatements(Resource subj,
			IRI pred, Value obj, Resource... contexts)
				throws QueryEvaluationException
	{
		return new EmptyIteration<Statement, QueryEvaluationException>();
	}
}
