/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.dataset;

import java.net.MalformedURLException;
import java.net.URL;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.Dataset;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.base.RepositoryConnectionWrapper;
import org.openrdf.repository.sail.SailBooleanQuery;
import org.openrdf.repository.sail.SailGraphQuery;
import org.openrdf.repository.sail.SailQuery;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.repository.sail.SailTupleQuery;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

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
			for (URI dataset : datasets.getDefaultGraphs()) {
				repository.loadDataset(new URL(dataset.toString()), dataset);
			}
			for (URI dataset : datasets.getNamedGraphs()) {
				repository.loadDataset(new URL(dataset.toString()), dataset);
			}
		}
		catch (MalformedURLException e) {
			throw new AssertionError(e);
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
		throw new IllegalArgumentException(q.getClass().getSimpleName() + " Not Supported on DatasetRepository");
	}

	private BooleanQuery wrap(final SailBooleanQuery q) {
		return new BooleanQuery() {

			public boolean evaluate()
				throws QueryEvaluationException
			{
				loadDataset(q.getActiveDataset());
				return q.evaluate();
			}

			public BindingSet getBindings() {
				return q.getBindings();
			}

			public Dataset getDataset() {
				return q.getDataset();
			}

			public boolean getIncludeInferred() {
				return q.getIncludeInferred();
			}

			public void removeBinding(String name) {
				q.removeBinding(name);
			}

			public void setBinding(String name, Value value) {
				q.setBinding(name, value);
			}

			public void setDataset(Dataset dataset) {
				q.setDataset(dataset);
			}

			public void setIncludeInferred(boolean includeInferred) {
				q.setIncludeInferred(includeInferred);
			}

			@Override
			public String toString() {
				return q.toString();
			}
		};
	}

	private GraphQuery wrap(final SailGraphQuery q) {
		return new GraphQuery() {

			public GraphQueryResult evaluate()
				throws QueryEvaluationException
			{
				loadDataset(q.getActiveDataset());
				return q.evaluate();
			}

			public void evaluate(RDFHandler handler)
				throws QueryEvaluationException, RDFHandlerException
			{
				loadDataset(q.getActiveDataset());
				q.evaluate(handler);
			}

			public BindingSet getBindings() {
				return q.getBindings();
			}

			public Dataset getDataset() {
				return q.getDataset();
			}

			public boolean getIncludeInferred() {
				return q.getIncludeInferred();
			}

			public void removeBinding(String name) {
				q.removeBinding(name);
			}

			public void setBinding(String name, Value value) {
				q.setBinding(name, value);
			}

			public void setDataset(Dataset dataset) {
				q.setDataset(dataset);
			}

			public void setIncludeInferred(boolean includeInferred) {
				q.setIncludeInferred(includeInferred);
			}

			@Override
			public String toString() {
				return q.toString();
			}
		};
	}

	private TupleQuery wrap(final SailTupleQuery q) {
		return new TupleQuery() {

			public TupleQueryResult evaluate()
				throws QueryEvaluationException
			{
				loadDataset(q.getActiveDataset());
				return q.evaluate();
			}

			public void evaluate(TupleQueryResultHandler handler)
				throws QueryEvaluationException, TupleQueryResultHandlerException
			{
				loadDataset(q.getActiveDataset());
				q.evaluate(handler);
			}

			public BindingSet getBindings() {
				return q.getBindings();
			}

			public Dataset getDataset() {
				return q.getDataset();
			}

			public boolean getIncludeInferred() {
				return q.getIncludeInferred();
			}

			public void removeBinding(String name) {
				q.removeBinding(name);
			}

			public void setBinding(String name, Value value) {
				q.setBinding(name, value);
			}

			public void setDataset(Dataset dataset) {
				q.setDataset(dataset);
			}

			public void setIncludeInferred(boolean includeInferred) {
				q.setIncludeInferred(includeInferred);
			}

			@Override
			public String toString() {
				return q.toString();
			}
		};
	}

}
