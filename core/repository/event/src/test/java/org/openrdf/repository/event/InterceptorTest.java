/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.event;

import static org.openrdf.query.QueryLanguage.SPARQL;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import junit.framework.TestCase;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.impl.AbstractUpdate;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.base.RepositoryConnectionWrapper;
import org.openrdf.repository.base.RepositoryWrapper;
import org.openrdf.repository.event.base.InterceptingRepositoryConnectionWrapper;
import org.openrdf.repository.event.base.RepositoryConnectionInterceptorAdapter;

/**
 * @author James Leigh
 */
public class InterceptorTest extends TestCase {

	static class InvocationHandlerStub implements InvocationHandler {

		public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable
		{
			if (Boolean.TYPE.equals(method.getReturnType()))
				return false;
			return null;
		}
	}

	static class RepositoryStub extends RepositoryWrapper {

		@Override
		public RepositoryConnection getConnection()
			throws RepositoryException
		{
			ClassLoader cl = InterceptorTest.class.getClassLoader();
			Class<?>[] classes = new Class[] { RepositoryConnection.class };
			InvocationHandlerStub handler = new InvocationHandlerStub();
			Object proxy = Proxy.newProxyInstance(cl, classes, handler);
			return (RepositoryConnection)proxy;
		}
	}

	static class RepositoryConnectionStub extends RepositoryConnectionWrapper {

		@Override
		protected boolean isDelegatingAdd() {
			return false;
		}

		@Override
		protected boolean isDelegatingRead() {
			return false;
		}

		@Override
		protected boolean isDelegatingRemove() {
			return false;
		}

		public RepositoryConnectionStub() throws RepositoryException {
			super(new RepositoryStub());
			setDelegate(getRepository().getConnection());
		}
	}

	static class UpdateStub extends AbstractUpdate implements Update {

		public void execute()
			throws UpdateExecutionException
		{
		}
	}

	public void testUpdate()
		throws Exception
	{
		final Update updateStub = new UpdateStub() {

			public void execute()
				throws UpdateExecutionException
			{
				fail();
			}
		};
		final RepositoryConnection stub = new RepositoryConnectionStub() {

			public Update prepareUpdate(QueryLanguage ql, String query, String baseURI)
				throws MalformedQueryException, RepositoryException
			{
				return updateStub;
			}
		};
		Repository repo = stub.getRepository();
		InterceptingRepositoryConnection con = new InterceptingRepositoryConnectionWrapper(repo, stub);
		con.addRepositoryConnectionInterceptor(new RepositoryConnectionInterceptorAdapter() {

			public boolean execute(RepositoryConnection conn, QueryLanguage ql, String update, String baseURI,
					Update operation)
			{
				assertEquals(stub, conn);
				assertEquals(SPARQL, ql);
				assertEquals("DELETE DATA { <> <> <> }", update);
				assertEquals("http://example.com/", baseURI);
				assertEquals(updateStub, operation);
				return true;
			}
		});
		Update update = con.prepareUpdate(SPARQL, "DELETE DATA { <> <> <> }", "http://example.com/");
		update.execute();
	}

	public void testRemove()
		throws Exception
	{
		ValueFactory vf = ValueFactoryImpl.getInstance();
		final URI uri = vf.createURI("http://example.com/");
		final RepositoryConnection stub = new RepositoryConnectionStub() {

			protected void removeWithoutCommit(Resource subject, URI predicate, Value object,
					Resource... contexts)
				throws RepositoryException
			{
				fail();
			}
		};
		Repository repo = stub.getRepository();
		InterceptingRepositoryConnection con = new InterceptingRepositoryConnectionWrapper(repo, stub);
		con.addRepositoryConnectionInterceptor(new RepositoryConnectionInterceptorAdapter() {

			public boolean remove(RepositoryConnection conn, Resource subject, URI predicate, Value object,
					Resource... contexts)
			{
				assertEquals(stub, conn);
				assertEquals(uri, subject);
				assertEquals(uri, predicate);
				assertEquals(uri, object);
				assertEquals(0, contexts.length);
				return true;
			}

		});
		con.remove(new StatementImpl(uri, uri, uri));
	}
}
