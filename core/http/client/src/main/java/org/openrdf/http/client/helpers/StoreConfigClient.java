/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client.helpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.http.client.connections.HTTPRequest;
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

	private final HTTPConnectionPool pool;

	public StoreConfigClient(HTTPConnectionPool pool) {
		this.pool = pool;
	}

	public String getURL() {
		return pool.getURL();
	}

	public void setUsernameAndPassword(String username, String password) {
		pool.setUsernameAndPassword(username, password);
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
		HTTPRequest request = pool.get();

		try {
			request.acceptTupleQueryResult();
			execute(request);
			request.readTupleQueryResult(handler);
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
			request.release();
		}
	}

	public <T> T get(Class<T> type)
		throws StoreConfigException
	{
		HTTPRequest request = pool.get();

		try {
			request.accept(type);
			execute(request);
			return request.read(type);
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
			request.release();
		}
	}

	public void put(Object instance)
		throws StoreConfigException
	{
		HTTPRequest request = pool.put();
		try {
			request.send(instance);
			execute(request);
		}
		catch (IOException e) {
			throw new StoreConfigException(e);
		}
		finally {
			request.release();
		}
	}

	public void delete()
		throws StoreConfigException
	{
		HTTPRequest request = pool.delete();
		try {
			execute(request);
		}
		catch (IOException e) {
			throw new StoreConfigException(e);
		}
		finally {
			request.release();
		}
	}

	public <T> T get(String id, Class<T> type)
		throws StoreConfigException
	{
		HTTPRequest request = pool.slash(id).get();

		try {
			request.accept(type);
			try {
				request.execute();
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
			return request.read(type);
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
			request.release();
		}
	}

	public void put(String id, Object instance)
		throws StoreConfigException
	{
		HTTPRequest request = pool.slash(id).put();
		try {
			request.send(instance);
			execute(request);
		}
		catch (IOException e) {
			throw new StoreConfigException(e);
		}
		finally {
			request.release();
		}
	}

	public boolean delete(String id)
		throws StoreConfigException
	{
		HTTPRequest request = pool.slash(id).delete();

		try {
			request.execute();
			return true;
		}
		catch (NotFound e) {
			return false;
		}
		catch (HTTPException e) {
			throw new StoreConfigException(e);
		}
		catch (IOException e) {
			throw new StoreConfigException(e);
		}
		finally {
			request.release();
		}
	}

	private void execute(HTTPRequest method)
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
		catch (HTTPException e) {
			throw new StoreConfigException(e);
		}
	}
}
