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
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.Dataset;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.impl.AbstractQuery;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.base.RepositoryConnectionWrapper;
import org.openrdf.repository.base.RepositoryWrapper;
import org.openrdf.rio.RDFHandler;
import org.openrdf.store.StoreException;

public class ContextAwareConnectionTest extends TestCase {

	static class GraphQueryStub extends AbstractQuery implements GraphQuery {

		public GraphResult evaluate() {
			return null;
		}

		public void evaluate(RDFHandler handler) {
		}
	}

	static class InvocationHandlerStub implements InvocationHandler {

		public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable
		{
			return null;
		}
	}

	static class QueryStub extends AbstractQuery {
	}

	static class RepositoryStub extends RepositoryWrapper {

		@Override
		public RepositoryConnection getConnection()
			throws StoreException
		{
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			Class<?>[] classes = new Class[] { RepositoryConnection.class };
			InvocationHandlerStub handler = new InvocationHandlerStub();
			Object proxy = Proxy.newProxyInstance(cl, classes, handler);
			return (RepositoryConnection)proxy;
		}
	}

	static class TupleQueryStub extends AbstractQuery implements TupleQuery {

		public TupleResult evaluate() {
			return null;
		}

		public void evaluate(TupleQueryResultHandler arg0) {
		}
	}

	private static class RepositoryConnectionStub extends RepositoryConnectionWrapper {

		public RepositoryConnectionStub() {
			super(new RepositoryStub());
		}
	}

	URI context = new URIImpl("urn:test:context");

	String queryString = "SELECT ?o WHERE { ?s ?p ?o}";

	public void testGraphQuery()
		throws Exception
	{
		RepositoryConnection stub = new RepositoryConnectionStub() {

			@Override
			public GraphQuery prepareGraphQuery(QueryLanguage ql, String query, String baseURI)
				throws MalformedQueryException, StoreException
			{
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
		con.prepareGraphQuery(SPARQL, queryString, null);
	}

	public void testQuery()
		throws Exception
	{
		RepositoryConnection stub = new RepositoryConnectionStub() {

			@Override
			public Query prepareQuery(QueryLanguage ql, String query, String baseURI)
				throws MalformedQueryException, StoreException
			{
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
		con.prepareQuery(SPARQL, queryString, null);
	}

	public void testTupleQuery()
		throws Exception
	{
		RepositoryConnection stub = new RepositoryConnectionStub() {

			@Override
			public TupleQuery prepareTupleQuery(QueryLanguage ql, String query, String baseURI)
				throws MalformedQueryException, StoreException
			{
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
		con.prepareTupleQuery(SPARQL, queryString, null);
	}
}
