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
package org.eclipse.rdf4j.repository.event;

import static org.eclipse.rdf4j.query.QueryLanguage.SPARQL;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.query.impl.AbstractUpdate;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.base.RepositoryConnectionWrapper;
import org.eclipse.rdf4j.repository.base.RepositoryWrapper;
import org.eclipse.rdf4j.repository.event.NotifyingRepositoryConnection;
import org.eclipse.rdf4j.repository.event.base.NotifyingRepositoryConnectionWrapper;
import org.eclipse.rdf4j.repository.event.base.RepositoryConnectionListenerAdapter;
import org.junit.Test;

/**
 * @author James Leigh
 */
public class NotifyingTest {

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
			ClassLoader cl = NotifyingTest.class.getClassLoader();
			Class<?>[] classes = new Class[] { RepositoryConnection.class };
			InvocationHandlerStub handler = new InvocationHandlerStub();
			Object proxy = Proxy.newProxyInstance(cl, classes, handler);
			return (RepositoryConnection)proxy;
		}
		
		@Override
		public ValueFactory getValueFactory() {
			return SimpleValueFactory.getInstance();
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

		public RepositoryConnectionStub()
			throws RepositoryException
		{
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

	@Test
	public void testUpdate()
		throws Exception
	{
		final Update updateStub = new UpdateStub();
		final RepositoryConnection stub = new RepositoryConnectionStub() {

			public Update prepareUpdate(QueryLanguage ql, String query, String baseURI)
				throws MalformedQueryException, RepositoryException
			{
				return updateStub;
			}
		};
		Repository repo = stub.getRepository();
		NotifyingRepositoryConnection con = new NotifyingRepositoryConnectionWrapper(repo, stub);
		con.addRepositoryConnectionListener(new RepositoryConnectionListenerAdapter() {

			public void execute(RepositoryConnection conn, QueryLanguage ql, String update, String baseURI,
					Update operation)
			{
				assertEquals(stub, conn);
				assertEquals(SPARQL, ql);
				assertEquals("DELETE DATA { <> <> <> }", update);
				assertEquals("http://example.com/", baseURI);
				assertEquals(updateStub, operation);
			}
		});
		Update update = con.prepareUpdate(SPARQL, "DELETE DATA { <> <> <> }", "http://example.com/");
		update.execute();
	}

	@Test
	public void testRemove()
		throws Exception
	{
		ValueFactory vf = SimpleValueFactory.getInstance();
		final IRI uri = vf.createIRI("http://example.com/");
		final RepositoryConnection stub = new RepositoryConnectionStub() {

			protected void removeWithoutCommit(Resource subject, IRI predicate, Value object,
					Resource... contexts)
						throws RepositoryException
			{
			}
		};
		Repository repo = stub.getRepository();
		NotifyingRepositoryConnection con = new NotifyingRepositoryConnectionWrapper(repo, stub);
		con.addRepositoryConnectionListener(new RepositoryConnectionListenerAdapter() {

			public void remove(RepositoryConnection conn, Resource subject, IRI predicate, Value object,
					Resource... contexts)
			{
				assertEquals(stub, conn);
				assertEquals(uri, subject);
				assertEquals(uri, predicate);
				assertEquals(uri, object);
				assertEquals(0, contexts.length);
			}

		});
		con.remove(con.getValueFactory().createStatement(uri, uri, uri));
	}
}