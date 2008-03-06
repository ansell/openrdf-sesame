/*
 * Copyright James Leigh (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;

import org.openrdf.model.ValueFactory;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.SailInitializationException;

/**
 * Repository that uses a Connection Proxy, redirecting to a Thread specific
 * Connection.
 * 
 * @author James Leigh <james@leighnet.ca>
 * 
 */
public class ThreadProxyRepository implements Repository {

	private static final Method closeMethod;
	private static final Method isOpenMethod;
	static {
		try {
			closeMethod = Connection.class.getDeclaredMethod("close");
			isOpenMethod = Connection.class.getDeclaredMethod("isOpen");
		} catch (SecurityException e) {
			throw new UndeclaredThrowableException(e);
		} catch (NoSuchMethodException e) {
			throw new UndeclaredThrowableException(e);
		}
	}

	private Repository repository;

	private Connection proxy;
	private ConnectionHandler handler = new ConnectionHandler();

	private class ConnectionHandler implements InvocationHandler {

		private ThreadLocal<Connection> localConnection = new ThreadLocal<Connection>();

		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			Connection conn = localConnection.get();
			if (closeMethod.equals(method)) {
				if (conn != null)
					conn.close();
				localConnection.set(null);
				return null;
			} else if (isOpenMethod.equals(method)) {
				return conn != null && conn.isOpen();
			} else {
				if (conn == null)
					localConnection.set(conn = repository.getConnection());
				return method.invoke(conn, args);
			}
		}

		public Connection getDelegateConnection() throws SailException {
			Connection conn = localConnection.get();
			if (conn == null)
				localConnection.set(conn = repository.getConnection());
			return conn;
		}

	}

	public ThreadProxyRepository(Sail sail) {
		this(new RepositoryImpl(sail));
	}

	public ThreadProxyRepository(Repository repository) {
		this.repository = repository;
		this.proxy = (Connection) Proxy.newProxyInstance(Connection.class
				.getClassLoader(), new Class[] { Connection.class }, handler);
		;
	}

	public Connection getConnection() throws SailException {
		// ensure Connection is created
		handler.getDelegateConnection();
		return proxy;
	}

	public Sail getSail() {
		return repository.getSail();
	}

	public ValueFactory getValueFactory() {
		return repository.getValueFactory();
	}

	public void initialize() throws SailInitializationException {
		repository.initialize();
	}

	public boolean isWritable() {
		return repository.isWritable();
	}

	public void shutDown() throws SailException {
		repository.shutDown();
	}

}
