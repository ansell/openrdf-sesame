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
package org.openrdf.spin.util;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ConvertingIteration;
import info.aduna.iteration.FilterIteration;
import info.aduna.iteration.Iteration;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.TripleSource;

/**
 * Useful methods for working with {@link TripleSource}s.
 */
public final class Statements {

	private Statements() {
	}

	public static Iteration<? extends Value, QueryEvaluationException> list(final Resource subj, final TripleSource store)
		throws QueryEvaluationException
	{
		return new Iteration<Value,QueryEvaluationException>() {
			Resource list = subj;

			@Override
			public boolean hasNext()
				throws QueryEvaluationException
			{
				return !RDF.NIL.equals(list);
			}

			@Override
			public Value next()
				throws QueryEvaluationException
			{
				Value v = singleValue(list, RDF.FIRST, store);
				list = (Resource) singleValue(list, RDF.REST, store);
				return v;
			}

			@Override
			public void remove()
				throws QueryEvaluationException
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	public static Value singleValue(Resource subj, URI pred, TripleSource store)
		throws QueryEvaluationException
	{
		Statement stmt = single(subj, pred, null, store);
		return (stmt != null) ? stmt.getObject() : null;
	}

	public static Statement single(Resource subj, URI pred, Value obj, TripleSource store)
		throws QueryEvaluationException
	{
		Statement stmt;
		CloseableIteration<? extends Statement, QueryEvaluationException> stmts = store.getStatements(subj,
				pred, obj);
		try {
			if (stmts.hasNext()) {
				stmt = stmts.next();
				if (stmts.hasNext()) {
					throw new QueryEvaluationException("Multiple statements for pattern: " + subj + " " + pred
							+ " " + obj);
				}
			}
			else {
				stmt = null;
			}
		}
		finally {
			stmts.close();
		}
		return stmt;
	}

	public static CloseableIteration<? extends URI, QueryEvaluationException> getSubjectURIs(URI predicate,
			Value object, TripleSource store)
		throws QueryEvaluationException
	{
		return new ConvertingIteration<Statement, URI, QueryEvaluationException>(
				new FilterIteration<Statement, QueryEvaluationException>(store.getStatements(null, predicate,
						object))
				{

					@Override
					protected boolean accept(Statement stmt)
						throws QueryEvaluationException
					{
						return (stmt.getSubject() instanceof URI);
					}
				})
		{

			@Override
			protected URI convert(Statement stmt)
				throws QueryEvaluationException
			{
				return (URI)stmt.getSubject();
			}
		};
	}

	public static CloseableIteration<? extends Resource, QueryEvaluationException> getObjectResources(
			Resource subject, URI predicate, TripleSource store)
		throws QueryEvaluationException
	{
		return new ConvertingIteration<Statement, Resource, QueryEvaluationException>(
				new FilterIteration<Statement, QueryEvaluationException>(store.getStatements(subject, predicate,
						null))
				{

					@Override
					protected boolean accept(Statement stmt)
						throws QueryEvaluationException
					{
						return (stmt.getObject() instanceof Resource);
					}
				})
		{

			@Override
			protected Resource convert(Statement stmt)
				throws QueryEvaluationException
			{
				return (Resource)stmt.getObject();
			}
		};
	}

	public static CloseableIteration<? extends URI, QueryEvaluationException> getObjectURIs(Resource subject,
			URI predicate, TripleSource store)
		throws QueryEvaluationException
	{
		return new ConvertingIteration<Statement, URI, QueryEvaluationException>(
				new FilterIteration<Statement, QueryEvaluationException>(store.getStatements(subject, predicate,
						null))
				{

					@Override
					protected boolean accept(Statement stmt)
						throws QueryEvaluationException
					{
						return (stmt.getObject() instanceof URI);
					}
				})
		{

			@Override
			protected URI convert(Statement stmt)
				throws QueryEvaluationException
			{
				return (URI)stmt.getObject();
			}
		};
	}

	public static CloseableIteration<? extends Literal, QueryEvaluationException> getObjectLiterals(
			Resource subject, URI predicate, TripleSource store)
		throws QueryEvaluationException
	{
		return new ConvertingIteration<Statement, Literal, QueryEvaluationException>(
				new FilterIteration<Statement, QueryEvaluationException>(store.getStatements(subject, predicate,
						null))
				{

					@Override
					protected boolean accept(Statement stmt)
						throws QueryEvaluationException
					{
						return (stmt.getObject() instanceof Literal);
					}
				})
		{

			@Override
			protected Literal convert(Statement stmt)
				throws QueryEvaluationException
			{
				return (Literal)stmt.getObject();
			}
		};
	}
}
