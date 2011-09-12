/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sparql;

import static org.openrdf.query.QueryLanguage.SPARQL;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;

import info.aduna.io.MavenUtil;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ConvertingIteration;
import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.ExceptionConvertingIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.SingletonIteration;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.query.Update;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sparql.query.SPARQLBooleanQuery;
import org.openrdf.repository.sparql.query.SPARQLGraphQuery;
import org.openrdf.repository.sparql.query.SPARQLTupleQuery;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

/**
 * Provides a {@link RepositoryConnection} interface to any SPARQL endpoint.
 * 
 * @author James Leigh
 * 
 */
public class SPARQLConnection extends ConnectionBase {
	private static final String EVERYTHING = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }";
	private static final String SOMETHING = "ASK { ?s ?p ?o }";
	private static final String NAMEDGRAPHS = "SELECT DISTINCT ?_ WHERE { GRAPH ?_ { ?s ?p ?o } }";
	private static final String APP_NAME = "OpenRDF.org SPARQLConnection";
	private static final String VERSION = MavenUtil.loadVersion(
			"org.openrdf.sesame", "sesame-repository-sparql", "devel");

	private HttpClient client;

	private String queryEndpointUrl;
	private String updateEndpointUrl;

	public SPARQLConnection(SPARQLRepository repository,
			String queryEndpointUrl, String updateEndpointUrl) {
		super(repository);
		this.queryEndpointUrl = queryEndpointUrl;
		this.updateEndpointUrl = updateEndpointUrl;

		// Use MultiThreadedHttpConnectionManager to allow concurrent access on
		// HttpClient
		HttpConnectionManager manager = new MultiThreadedHttpConnectionManager();

		// Allow 20 concurrent connections to the same host (default is 2)
		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setDefaultMaxConnectionsPerHost(20);
		params.setStaleCheckingEnabled(false);
		manager.setParams(params);

		HttpClientParams clientParams = new HttpClientParams();
		clientParams.setParameter(
				HttpMethodParams.USER_AGENT,
				APP_NAME
						+ "/"
						+ VERSION
						+ " "
						+ clientParams
								.getParameter(HttpMethodParams.USER_AGENT));
		client = new HttpClient(clientParams, manager);
	}

	@Override
	public String toString() {
		return queryEndpointUrl;
	}

	public void exportStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred, RDFHandler handler, Resource... contexts)
			throws RepositoryException, RDFHandlerException {
		try {
			GraphQuery query = prepareGraphQuery(SPARQL, EVERYTHING, "");
			setBindings(query, subj, pred, obj, contexts);
			query.evaluate(handler);
		} catch (MalformedQueryException e) {
			throw new RepositoryException(e);
		} catch (QueryEvaluationException e) {
			throw new RepositoryException(e);
		}
	}

	public RepositoryResult<Resource> getContextIDs()
			throws RepositoryException {
		try {
			TupleQuery query = prepareTupleQuery(SPARQL, NAMEDGRAPHS, "");
			TupleQueryResult result = query.evaluate();
			return new RepositoryResult<Resource>(
					new ExceptionConvertingIteration<Resource, RepositoryException>(
							new ConvertingIteration<BindingSet, Resource, QueryEvaluationException>(
									result) {

								@Override
								protected Resource convert(BindingSet bindings)
										throws QueryEvaluationException {
									return (Resource) bindings.getValue("_");
								}
							}) {

						@Override
						protected RepositoryException convert(Exception e) {
							return new RepositoryException(e);
						}
					});
		} catch (MalformedQueryException e) {
			throw new RepositoryException(e);
		} catch (QueryEvaluationException e) {
			throw new RepositoryException(e);
		}
	}

	public RepositoryResult<Statement> getStatements(Resource subj, URI pred,
			Value obj, boolean includeInferred, Resource... contexts)
			throws RepositoryException {
		try {
			if (subj != null && pred != null && obj != null) {
				if (hasStatement(subj, pred, obj, includeInferred, contexts)) {
					Statement st = new StatementImpl(subj, pred, obj);
					CloseableIteration<Statement, RepositoryException> cursor;
					cursor = new SingletonIteration<Statement, RepositoryException>(
							st);
					return new RepositoryResult<Statement>(cursor);
				} else {
					return new RepositoryResult<Statement>(
							new EmptyIteration<Statement, RepositoryException>());
				}
			}
			GraphQuery query = prepareGraphQuery(SPARQL, EVERYTHING, "");
			setBindings(query, subj, pred, obj, contexts);
			GraphQueryResult result = query.evaluate();
			return new RepositoryResult<Statement>(
					new ExceptionConvertingIteration<Statement, RepositoryException>(
							result) {

						@Override
						protected RepositoryException convert(Exception e) {
							return new RepositoryException(e);
						}
					});
		} catch (MalformedQueryException e) {
			throw new RepositoryException(e);
		} catch (QueryEvaluationException e) {
			throw new RepositoryException(e);
		}
	}

	public boolean hasStatement(Resource subj, URI pred, Value obj,
			boolean includeInferred, Resource... contexts)
			throws RepositoryException {
		try {
			BooleanQuery query = prepareBooleanQuery(SPARQL, SOMETHING, "");
			setBindings(query, subj, pred, obj, contexts);
			return query.evaluate();
		} catch (MalformedQueryException e) {
			throw new RepositoryException(e);
		} catch (QueryEvaluationException e) {
			throw new RepositoryException(e);
		}
	}

	public Query prepareQuery(QueryLanguage ql, String query, String base)
			throws RepositoryException, MalformedQueryException {
		// TODO implement properly, taking subqueries into account
		throw new UnsupportedOperationException();
	}

	public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String query,
			String base) throws RepositoryException, MalformedQueryException {
		if (SPARQL.equals(ql))
			return new SPARQLBooleanQuery(client, queryEndpointUrl, base, query);
		throw new UnsupportedQueryLanguageException(
				"Unsupported query language " + ql);
	}

	public GraphQuery prepareGraphQuery(QueryLanguage ql, String query,
			String base) throws RepositoryException, MalformedQueryException {
		if (SPARQL.equals(ql))
			return new SPARQLGraphQuery(client, queryEndpointUrl, base, query);
		throw new UnsupportedQueryLanguageException(
				"Unsupported query language " + ql);
	}

	public TupleQuery prepareTupleQuery(QueryLanguage ql, String query,
			String base) throws RepositoryException, MalformedQueryException {
		if (SPARQL.equals(ql))
			return new SPARQLTupleQuery(client, queryEndpointUrl, base, query);
		throw new UnsupportedQueryLanguageException(
				"Unsupported query language " + ql);
	}
	

	public void commit() throws RepositoryException {
		// no-op
	}

	public boolean isAutoCommit() throws RepositoryException {
		return true;
	}

	public void rollback() throws RepositoryException {
		// no-op
	}

	public void setAutoCommit(boolean autoCommit) throws RepositoryException {
		if (!autoCommit) {
			throw new UnsupportedOperationException();
		}
	}

	public void add(Statement st, Resource... contexts)
			throws RepositoryException {
		
	}

	public void add(Iterable<? extends Statement> statements,
			Resource... contexts) throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	public <E extends Exception> void add(
			Iteration<? extends Statement, E> statementIter,
			Resource... contexts) throws RepositoryException, E {
		throw new UnsupportedOperationException();
	}

	public void add(InputStream in, String baseURI, RDFFormat dataFormat,
			Resource... contexts) throws IOException, RDFParseException,
			RepositoryException {
		throw new UnsupportedOperationException();
	}

	public void add(Reader reader, String baseURI, RDFFormat dataFormat,
			Resource... contexts) throws IOException, RDFParseException,
			RepositoryException {
		throw new UnsupportedOperationException();
	}

	public void add(URL url, String baseURI, RDFFormat dataFormat,
			Resource... contexts) throws IOException, RDFParseException,
			RepositoryException {
		throw new UnsupportedOperationException();
	}

	public void add(File file, String baseURI, RDFFormat dataFormat,
			Resource... contexts) throws IOException, RDFParseException,
			RepositoryException {
		throw new UnsupportedOperationException();
	}

	public void add(Resource subject, URI predicate, Value object,
			Resource... contexts) throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	public void clear(Resource... contexts) throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	public void clearNamespaces() throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	public void remove(Statement st, Resource... contexts)
			throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	public void remove(Iterable<? extends Statement> statements,
			Resource... contexts) throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	public <E extends Exception> void remove(
			Iteration<? extends Statement, E> statementIter,
			Resource... contexts) throws RepositoryException, E {
		throw new UnsupportedOperationException();
	}

	public void remove(Resource subject, URI predicate, Value object,
			Resource... contexts) throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	public void removeNamespace(String prefix) throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	public void setNamespace(String prefix, String name)
			throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	public Update prepareUpdate(QueryLanguage ql, String update)
			throws RepositoryException, MalformedQueryException {
		throw new UnsupportedOperationException();
	}

	public Update prepareUpdate(QueryLanguage ql, String update, String baseURI)
			throws RepositoryException, MalformedQueryException {
		throw new UnsupportedOperationException();
	}

	private void setBindings(Query query, Resource subj, URI pred, Value obj,
			Resource... contexts) throws RepositoryException {
		if (subj != null) {
			query.setBinding("s", subj);
		}
		if (pred != null) {
			query.setBinding("p", pred);
		}
		if (obj != null) {
			query.setBinding("o", obj);
		}
		if (contexts != null && contexts.length > 0
				&& (contexts[0] != null || contexts.length > 1)) {
			DatasetImpl dataset = new DatasetImpl();
			for (Resource ctx : contexts) {
				if (ctx instanceof URI) {
					dataset.addDefaultGraph((URI) ctx);
				} else {
					throw new RepositoryException("Contexts must be URIs");
				}
			}
			query.setDataset(dataset);
		}
	}
}
