/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client.helpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.http.client.connections.HTTPConnection;
import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.protocol.exceptions.HTTPException;
import org.openrdf.http.protocol.exceptions.NoCompatibleMediaType;
import org.openrdf.http.protocol.exceptions.NotFound;
import org.openrdf.http.protocol.exceptions.Unauthorized;
import org.openrdf.http.protocol.exceptions.UnsupportedFileFormat;
import org.openrdf.http.protocol.exceptions.UnsupportedMediaType;
import org.openrdf.http.protocol.exceptions.UnsupportedQueryLanguage;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerBase;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.store.StoreConfigException;

/**
 * @author James Leigh
 */
public class StoreConfigClient {

	private HTTPConnectionPool server;

	public StoreConfigClient(HTTPConnectionPool server) {
		this.server = server;
	}

	public String getURL() {
		return server.getURL();
	}

	public void setUsernameAndPassword(String username, String password) {
		server.setUsernameAndPassword(username, password);
	}

	public List<String> list()
		throws StoreConfigException
	{
		try {
			final List<String> list = new ArrayList<String>();
			list(new TupleQueryResultHandlerBase() {

				@Override
				public void handleSolution(BindingSet bindings)
					throws TupleQueryResultHandlerException
				{
					list.add(bindings.getValue("id").stringValue());
				}
			});
			return list;
		}
		catch (TupleQueryResultHandlerException e) {
			throw new AssertionError(e);
		}
	}

	public void list(TupleQueryResultHandler handler)
		throws TupleQueryResultHandlerException, StoreConfigException
	{
		HTTPConnection method = server.get();

		try {
			method.acceptTupleQueryResult();
			execute(method);
			method.readTupleQueryResult(handler);
		}
		catch (IOException e) {
			throw new StoreConfigException(e);
		}
		catch (NoCompatibleMediaType e) {
			throw new StoreConfigException(e);
		}
		catch (QueryResultParseException e) {
			throw new StoreConfigException(e);
		}
		finally {
			method.release();
		}
	}

	public <T> T get(Class<T> type)
		throws StoreConfigException
	{
		HTTPConnection method = server.get();

		try {
			method.accept(type);
			execute(method);
			return method.read(type);
		}
		catch (IOException e) {
			throw new StoreConfigException(e);
		}
		catch (NoCompatibleMediaType e) {
			throw new StoreConfigException(e);
		}
		catch (NumberFormatException e) {
			throw new StoreConfigException(e);
		}
		catch (QueryResultParseException e) {
			throw new StoreConfigException(e);
		}
		catch (RDFParseException e) {
			throw new StoreConfigException(e);
		}
		finally {
			method.release();
		}
	}

	public void put(Object instance)
		throws StoreConfigException
	{
		HTTPConnection method = server.put();
		try {
			method.send(instance);
			execute(method);
		}
		catch (IOException e) {
			throw new StoreConfigException(e);
		}
		finally {
			method.release();
		}
	}

	public void post(Object instance)
		throws StoreConfigException
	{
		HTTPConnection method = server.post();
		try {
			method.send(instance);
			execute(method);
		}
		catch (IOException e) {
			throw new StoreConfigException(e);
		}
		finally {
			method.release();
		}
	}

	public void delete()
		throws StoreConfigException
	{
		HTTPConnection method = server.delete();
		try {
			execute(method);
		}
		catch (IOException e) {
			throw new StoreConfigException(e);
		}
		finally {
			method.release();
		}
	}

	public <T> T get(String id, Class<T> type)
		throws StoreConfigException
	{
		HTTPConnection method = server.slash(id).get();

		try {
			method.accept(type);
			try {
				method.execute();
			}
			catch (NotFound e) {
				return null;
			}
			catch (UnsupportedQueryLanguage e) {
				throw new UnsupportedQueryLanguageException(e);
			}
			catch (UnsupportedFileFormat e) {
				throw new UnsupportedRDFormatException(e);
			}
			catch (UnsupportedMediaType e) {
				throw new UnsupportedRDFormatException(e);
			}
			catch (Unauthorized e) {
				throw new StoreConfigException(e);
			}
			catch (HTTPException e) {
				throw new StoreConfigException(e);
			}
			return method.read(type);
		}
		catch (IOException e) {
			throw new StoreConfigException(e);
		}
		catch (NumberFormatException e) {
			throw new StoreConfigException(e);
		}
		catch (QueryResultParseException e) {
			throw new StoreConfigException(e);
		}
		catch (RDFParseException e) {
			throw new StoreConfigException(e);
		}
		catch (NoCompatibleMediaType e) {
			throw new StoreConfigException(e);
		}
		finally {
			method.release();
		}
	}

	public void put(String id, Object instance)
		throws StoreConfigException
	{
		HTTPConnection method = server.slash(id).put();
		try {
			method.send(instance);
			execute(method);
		}
		catch (IOException e) {
			throw new StoreConfigException(e);
		}
		finally {
			method.release();
		}
	}

	public void delete(String id)
		throws StoreConfigException
	{
		HTTPConnection method = server.slash(id).delete();
		try {
			execute(method);
		}
		catch (IOException e) {
			throw new StoreConfigException(e);
		}
		finally {
			method.release();
		}
	}

	private void execute(HTTPConnection method)
		throws IOException, StoreConfigException
	{
		try {
			method.execute();
		}
		catch (UnsupportedQueryLanguage e) {
			throw new UnsupportedQueryLanguageException(e);
		}
		catch (UnsupportedFileFormat e) {
			throw new UnsupportedRDFormatException(e);
		}
		catch (Unauthorized e) {
			throw new StoreConfigException(e);
		}
		catch (HTTPException e) {
			throw new StoreConfigException(e);
		}
	}

}
