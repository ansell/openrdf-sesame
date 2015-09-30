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
