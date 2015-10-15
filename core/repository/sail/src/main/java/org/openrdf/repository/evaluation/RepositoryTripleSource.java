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
package org.openrdf.repository;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ExceptionConvertingIteration;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.TripleSource;

public class RepositoryTripleSource implements TripleSource {

	private final RepositoryConnection repo;

	private final boolean includeInferred;

	public RepositoryTripleSource(RepositoryConnection repo, boolean includeInferred) {
		this.repo = repo;
		this.includeInferred = includeInferred;
	}

	@Override
	public CloseableIteration<? extends Statement, QueryEvaluationException> getStatements(Resource subj,
			URI pred, Value obj, Resource... contexts)
		throws QueryEvaluationException
	{
		RepositoryResult<Statement> result;
		try {
			result = repo.getStatements(subj, pred, obj, includeInferred, contexts);
		}
		catch (RepositoryException e) {
			throw new QueryEvaluationException(e);
		}
		return new ExceptionConvertingIteration<Statement, QueryEvaluationException>(result) {

			@Override
			protected QueryEvaluationException convert(Exception exception) {
				return new QueryEvaluationException(exception);
			}
		};
	}

	@Override
	public ValueFactory getValueFactory() {
		return repo.getValueFactory();
	}

}
