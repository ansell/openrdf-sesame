/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.dataset;

import java.net.MalformedURLException;
import java.net.URL;

import org.openrdf.model.URI;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.Dataset;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.repository.base.RepositoryConnectionWrapper;
import org.openrdf.repository.sail.SailBooleanQuery;
import org.openrdf.repository.sail.SailGraphQuery;
import org.openrdf.repository.sail.SailQuery;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.repository.sail.SailTupleQuery;
import org.openrdf.store.StoreException;

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
		throws MalformedQueryException, StoreException
	{
		return wrap(delegate.prepareBooleanQuery(ql, query, baseURI));
	}

	@Override
	public GraphQuery prepareGraphQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, StoreException
	{
		return wrap(delegate.prepareGraphQuery(ql, query, baseURI));
	}

	@Override
	public Query prepareQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, StoreException
	{
		return wrap(delegate.prepareQuery(ql, query, baseURI));
	}

	@Override
	public TupleQuery prepareTupleQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, StoreException
	{
		return wrap(delegate.prepareTupleQuery(ql, query, baseURI));
	}

	void loadDataset(Dataset datasets)
		throws StoreException
	{
		try {
			if (datasets == null) {
				return;
			}
			for (URI dataset : datasets.getDefaultGraphs()) {
				repository.loadDataset(new URL(dataset.toString()), dataset);
			}
			for (URI dataset : datasets.getNamedGraphs()) {
				repository.loadDataset(new URL(dataset.toString()), dataset);
			}
		}
		catch (MalformedURLException e) {
			throw new StoreException(e);
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
