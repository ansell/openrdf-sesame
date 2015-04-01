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
package org.openrdf.repository.dataset;

import java.net.MalformedURLException;
import java.net.URL;

import org.openrdf.model.IRI;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.Dataset;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.base.RepositoryConnectionWrapper;
import org.openrdf.repository.sail.SailBooleanQuery;
import org.openrdf.repository.sail.SailGraphQuery;
import org.openrdf.repository.sail.SailQuery;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.repository.sail.SailTupleQuery;

public class DatasetRepositoryConnection extends RepositoryConnectionWrapper {

	private DatasetRepository repository;

	private SailRepositoryConnection delegate;

	public DatasetRepositoryConnection(DatasetRepository repository, SailRepositoryConnection delegate) {
		super(repository, delegate);
		this.repository = repository;
		this.delegate = delegate;
	}

	@Override
	public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, RepositoryException
	{
		return wrap(delegate.prepareBooleanQuery(ql, query, baseURI));
	}

	@Override
	public GraphQuery prepareGraphQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, RepositoryException
	{
		return wrap(delegate.prepareGraphQuery(ql, query, baseURI));
	}

	@Override
	public Query prepareQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, RepositoryException
	{
		return wrap(delegate.prepareQuery(ql, query, baseURI));
	}

	@Override
	public TupleQuery prepareTupleQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, RepositoryException
	{
		return wrap(delegate.prepareTupleQuery(ql, query, baseURI));
	}

	void loadDataset(Dataset datasets)
		throws QueryEvaluationException
	{
		try {
			if (datasets == null) {
				return;
			}
			for (IRI dataset : datasets.getDefaultGraphs()) {
				repository.loadDataset(new URL(dataset.toString()), dataset, getParserConfig());
			}
			for (IRI dataset : datasets.getNamedGraphs()) {
				repository.loadDataset(new URL(dataset.toString()), dataset, getParserConfig());
			}
		}
		catch (MalformedURLException e) {
			throw new QueryEvaluationException(e);
		}
		catch (RepositoryException e) {
			throw new QueryEvaluationException(e);
		}
	}

	private Query wrap(SailQuery q) {
		if (q instanceof SailBooleanQuery) {
			return wrap((SailBooleanQuery)q);
		}
		if (q instanceof SailGraphQuery) {
			return wrap((SailGraphQuery)q);
		}
		if (q instanceof SailTupleQuery) {
			return wrap((SailTupleQuery)q);
		}
		throw new IllegalArgumentException(q.getClass().getSimpleName() + " not supported on DatasetRepository");
	}

	private BooleanQuery wrap(final SailBooleanQuery q) {
		return new DatasetBooleanQuery(this, q);
	}

	private GraphQuery wrap(final SailGraphQuery q) {
		return new DatasetGraphQuery(this, q);
	}

	private TupleQuery wrap(final SailTupleQuery q) {
		return new DatasetTupleQuery(this, q);
	}
}
