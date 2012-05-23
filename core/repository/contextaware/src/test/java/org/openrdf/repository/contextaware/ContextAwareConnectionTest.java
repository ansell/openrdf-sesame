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
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.impl.AbstractQuery;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.base.RepositoryConnectionWrapper;
import org.openrdf.repository.base.RepositoryWrapper;
import org.openrdf.rio.RDFHandler;

public class ContextAwareConnectionTest extends TestCase {
	static class GraphQueryStub extends AbstractQuery implements
			GraphQuery {
		public GraphQueryResult evaluate() {
			return null;
		}

		public void evaluate(RDFHandler arg0) {
		}
	}

	static class InvocationHandlerStub implements InvocationHandler {
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			return null;
		}
	}

	static class QueryStub extends AbstractQuery {
	}

	static class RepositoryStub extends RepositoryWrapper {
		@Override
		public RepositoryConnection getConnection() throws RepositoryException {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			Class<?>[] classes = new Class[] { RepositoryConnection.class };
			InvocationHandlerStub handler = new InvocationHandlerStub();
			Object proxy = Proxy.newProxyInstance(cl, classes, handler);
			return (RepositoryConnection) proxy;
		}
	}

	static class TupleQueryStub extends AbstractQuery implements
			TupleQuery {
		public TupleQueryResult evaluate() {
			return null;
		}

		public void evaluate(TupleQueryResultHandler arg0) {
		}
	}

	private static class RepositoryConnectionStub extends
			RepositoryConnectionWrapper {
		public RepositoryConnectionStub() {
			super(new RepositoryStub());
		}
	}

	URI context = new URIImpl("urn:test:context");

	String queryString = "SELECT ?o WHERE { ?s ?p ?o}";

	public void testGraphQuery() throws Exception {
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
		con.prepareGraphQuery(SPARQL, queryString, null);
	}

	public void testQuery() throws Exception {
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
		con.prepareQuery(SPARQL, queryString, null);
	}

	public void testTupleQuery() throws Exception {
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
		con.prepareTupleQuery(SPARQL, queryString, null);
	}

	public void testIncludeInferred() throws Exception {
		RepositoryConnection stub = new RepositoryConnectionStub();
		Repository repo = stub.getRepository();
		ContextAwareConnection a = new ContextAwareConnection(repo, stub);
		ContextAwareConnection b = new ContextAwareConnection(repo, a);
		b.setIncludeInferred(true);
		assertTrue(b.isIncludeInferred());
		assertTrue(a.isIncludeInferred());
	}

	public void testMaxQueryTime() throws Exception {
		RepositoryConnection stub = new RepositoryConnectionStub();
		Repository repo = stub.getRepository();
		ContextAwareConnection a = new ContextAwareConnection(repo, stub);
		ContextAwareConnection b = new ContextAwareConnection(repo, a);
		b.setMaxQueryTime(1);
		assertEquals(1, b.getMaxQueryTime());
		assertEquals(1, a.getMaxQueryTime());
	}

	public void testQueryLanguage() throws Exception {
		RepositoryConnection stub = new RepositoryConnectionStub();
		Repository repo = stub.getRepository();
		ContextAwareConnection a = new ContextAwareConnection(repo, stub);
		ContextAwareConnection b = new ContextAwareConnection(repo, a);
		b.setQueryLanguage(QueryLanguage.SERQL);
		assertEquals(QueryLanguage.SERQL, b.getQueryLanguage());
		assertEquals(QueryLanguage.SERQL, a.getQueryLanguage());
	}

	public void testBaseURI() throws Exception {
		RepositoryConnection stub = new RepositoryConnectionStub();
		Repository repo = stub.getRepository();
		ContextAwareConnection a = new ContextAwareConnection(repo, stub);
		ContextAwareConnection b = new ContextAwareConnection(repo, a);
		b.setBaseURI("http://example.com/");
		assertEquals("http://example.com/", b.getBaseURI());
		assertEquals("http://example.com/", a.getBaseURI());
	}

	public void testReadContexts() throws Exception {
		RepositoryConnection stub = new RepositoryConnectionStub();
		Repository repo = stub.getRepository();
		ContextAwareConnection a = new ContextAwareConnection(repo, stub);
		ContextAwareConnection b = new ContextAwareConnection(repo, a);
		b.setReadContexts(context);
		assertEquals(context, b.getReadContexts()[0]);
		assertEquals(context, a.getReadContexts()[0]);
	}

	public void testRemoveContexts() throws Exception {
		RepositoryConnection stub = new RepositoryConnectionStub();
		Repository repo = stub.getRepository();
		ContextAwareConnection a = new ContextAwareConnection(repo, stub);
		ContextAwareConnection b = new ContextAwareConnection(repo, a);
		b.setRemoveContexts(context);
		assertEquals(context, b.getRemoveContexts()[0]);
		assertEquals(context, a.getRemoveContexts()[0]);
	}

	public void testAddContexts() throws Exception {
		RepositoryConnection stub = new RepositoryConnectionStub();
		Repository repo = stub.getRepository();
		ContextAwareConnection a = new ContextAwareConnection(repo, stub);
		ContextAwareConnection b = new ContextAwareConnection(repo, a);
		b.setAddContexts(context);
		assertEquals(context, b.getAddContexts()[0]);
		assertEquals(context, a.getAddContexts()[0]);
	}

	public void testArchiveContexts() throws Exception {
		RepositoryConnection stub = new RepositoryConnectionStub();
		Repository repo = stub.getRepository();
		ContextAwareConnection a = new ContextAwareConnection(repo, stub);
		ContextAwareConnection b = new ContextAwareConnection(repo, a);
		b.setArchiveContexts(context);
		assertEquals(context, b.getArchiveContexts()[0]);
		assertEquals(context, a.getArchiveContexts()[0]);
	}

	public void testInsertContexts() throws Exception {
		RepositoryConnection stub = new RepositoryConnectionStub();
		Repository repo = stub.getRepository();
		ContextAwareConnection a = new ContextAwareConnection(repo, stub);
		ContextAwareConnection b = new ContextAwareConnection(repo, a);
		b.setInsertContext(context);
		assertEquals(context, b.getInsertContext());
		assertEquals(context, a.getInsertContext());
	}
}
