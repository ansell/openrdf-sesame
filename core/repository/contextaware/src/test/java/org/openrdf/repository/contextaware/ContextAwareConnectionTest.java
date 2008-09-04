package org.openrdf.repository.contextaware;

import static org.openrdf.query.QueryLanguage.SERQL;
import static org.openrdf.query.QueryLanguage.SPARQL;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Set;

import junit.framework.TestCase;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
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
import org.openrdf.query.impl.AbstractQuery;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.base.RepositoryConnectionWrapper;
import org.openrdf.repository.base.RepositoryWrapper;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

public class ContextAwareConnectionTest extends TestCase {
	private static class GraphQueryStub extends AbstractQuery implements
			GraphQuery {
		public GraphQueryResult evaluate() throws QueryEvaluationException {
			return null;
		}

		public void evaluate(RDFHandler arg0) throws QueryEvaluationException,
				RDFHandlerException {
		}
	}
	private static class InvocationHandlerStub implements InvocationHandler {
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			return null;
		}
	}
	private static class QueryStub extends AbstractQuery {
	}

	private static class RepositoryConnectionStub extends
			RepositoryConnectionWrapper {
		public RepositoryConnectionStub() {
			super(new RepositoryStub());
		}
	}

	private static class RepositoryStub extends RepositoryWrapper {
		@Override
		public RepositoryConnection getConnection() throws RepositoryException {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			Class[] classes = new Class[] { RepositoryConnection.class };
			InvocationHandlerStub handler = new InvocationHandlerStub();
			Object proxy = Proxy.newProxyInstance(cl, classes, handler);
			return (RepositoryConnection) proxy;
		}
	}

	private static class TupleQueryStub extends AbstractQuery implements
			TupleQuery {
		public TupleQueryResult evaluate() throws QueryEvaluationException {
			return null;
		}

		public void evaluate(TupleQueryResultHandler arg0)
				throws QueryEvaluationException,
				TupleQueryResultHandlerException {
		}
	}

	private ValueFactory vf = new ValueFactoryImpl();

	private URI context = vf.createURI("urn:test:context");

	private String queryString = "SELECT ?o WHERE { ?s ?p ?o}";

	public void testGraphQueryWithinContext() throws Exception {
		RepositoryConnection stub = new RepositoryConnectionStub() {
			@Override
			public GraphQuery prepareGraphQuery(QueryLanguage ql, String query,
					String baseURI) throws MalformedQueryException,
					RepositoryException {
				assertEquals(SPARQL, ql);
				assertEquals(queryString, query);
				return new GraphQueryStub() {
					@Override
					public void setDataset(Dataset dataset) {
						Set<URI> contexts = Collections.singleton(context);
						assertEquals(contexts, dataset.getDefaultGraphs());
						super.setDataset(dataset);
					}
				};
			}
		};
		Repository repo = stub.getRepository();
		ContextAwareConnection con = new ContextAwareConnection(repo, stub);
		con.setReadContexts(context);
		con.setQueryLanguage(SERQL);
		con.prepareGraphQueryWithinContext(SPARQL, queryString, null);
	}

	public void testQueryWithinContext() throws Exception {
		RepositoryConnection stub = new RepositoryConnectionStub() {
			@Override
			public Query prepareQuery(QueryLanguage ql, String query,
					String baseURI) throws MalformedQueryException,
					RepositoryException {
				assertEquals(SPARQL, ql);
				assertEquals(queryString, query);
				return new QueryStub() {
					@Override
					public void setDataset(Dataset dataset) {
						Set<URI> contexts = Collections.singleton(context);
						assertEquals(contexts, dataset.getDefaultGraphs());
						super.setDataset(dataset);
					}
				};
			}
		};
		Repository repo = stub.getRepository();
		ContextAwareConnection con = new ContextAwareConnection(repo, stub);
		con.setReadContexts(context);
		con.setQueryLanguage(SERQL);
		con.prepareQueryWithinContext(SPARQL, queryString, null);
	}

	public void testTupleQueryWithinContext() throws Exception {
		RepositoryConnection stub = new RepositoryConnectionStub() {
			@Override
			public TupleQuery prepareTupleQuery(QueryLanguage ql, String query,
					String baseURI) throws MalformedQueryException,
					RepositoryException {
				assertEquals(SPARQL, ql);
				assertEquals(queryString, query);
				return new TupleQueryStub() {
					@Override
					public void setDataset(Dataset dataset) {
						Set<URI> contexts = Collections.singleton(context);
						assertEquals(contexts, dataset.getDefaultGraphs());
						super.setDataset(dataset);
					}
				};
			}
		};
		Repository repo = stub.getRepository();
		ContextAwareConnection con = new ContextAwareConnection(repo, stub);
		con.setReadContexts(context);
		con.setQueryLanguage(SERQL);
		con.prepareTupleQueryWithinContext(SPARQL, queryString, null);
	}
}
