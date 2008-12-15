/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client.helpers;

import java.io.IOException;

import org.openrdf.http.client.connections.HTTPConnection;
import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.protocol.UnauthorizedException;
import org.openrdf.http.protocol.exceptions.HTTPException;
import org.openrdf.http.protocol.exceptions.NoCompatibleMediaType;
import org.openrdf.http.protocol.exceptions.NotFound;
import org.openrdf.http.protocol.exceptions.Unauthorized;
import org.openrdf.http.protocol.exceptions.UnsupportedFileFormat;
import org.openrdf.http.protocol.exceptions.UnsupportedMediaType;
import org.openrdf.http.protocol.exceptions.UnsupportedQueryLanguage;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.results.TupleResult;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.store.StoreException;


/**
 *
 * @author James Leigh
 */
public class StoreClient {

	private HTTPConnectionPool server;

	public StoreClient(HTTPConnectionPool server) {
		this.server = server;
	}

	public TupleResult list()
		throws StoreException
	{
		HTTPConnection method = server.get();

		try {
			method.acceptTupleQueryResult();
			execute(method);
			return method.getTupleQueryResult();
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		catch (NoCompatibleMediaType e) {
			throw new StoreException(e);
		}
		catch (QueryResultParseException e) {
			throw new StoreException(e);
		}
	}

	public String create()
		throws StoreException
	{
		HTTPConnection method = server.post();
		try {
			execute(method);
			return method.readLocation();
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			method.release();
		}
	}

	public <T> T get(Class<T> type)
		throws StoreException
	{
		HTTPConnection method = server.get();

		try {
			method.accept(type);
			execute(method);
			return method.read(type);
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		catch (NoCompatibleMediaType e) {
			throw new StoreException(e);
		}
		catch (NumberFormatException e) {
			throw new StoreException(e);
		}
		catch (QueryResultParseException e) {
			throw new StoreException(e);
		}
		catch (RDFParseException e) {
			throw new StoreException(e);
		}
		finally {
			method.release();
		}
	}

	public void post()
		throws StoreException
	{
		HTTPConnection method = server.post();
		try {
			execute(method);
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			method.release();
		}
	}

	public void put(Object instance)
		throws StoreException
	{
		HTTPConnection method = server.put();
		try {
			method.send(instance);
			execute(method);
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			method.release();
		}
	}

	public void post(Object instance)
		throws StoreException
	{
		HTTPConnection method = server.post();
		try {
			method.send(instance);
			execute(method);
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			method.release();
		}
	}

	public void delete()
		throws StoreException
	{
		HTTPConnection method = server.delete();
		try {
			execute(method);
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			method.release();
		}
	}

	public <T> T get(String id, Class<T> type)
		throws StoreException
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
				throw new UnauthorizedException(e);
			}
			catch (HTTPException e) {
				throw new StoreException(e);
			}
			return method.read(type);
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		catch (NumberFormatException e) {
			throw new StoreException(e);
		}
		catch (QueryResultParseException e) {
			throw new StoreException(e);
		}
		catch (RDFParseException e) {
			throw new StoreException(e);
		}
		catch (NoCompatibleMediaType e) {
			throw new StoreException(e);
		}
		finally {
			method.release();
		}
	}

	public void put(String id, Object instance)
		throws StoreException
	{
		HTTPConnection method = server.slash(id).put();
		try {
			method.send(instance);
			execute(method);
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			method.release();
		}
	}

	public void delete(String id)
		throws StoreException
	{
		HTTPConnection method = server.slash(id).delete();
		try {
			execute(method);
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			method.release();
		}
	}

	private void execute(HTTPConnection method)
		throws IOException, StoreException
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
			throw new UnauthorizedException(e);
		}
		catch (HTTPException e) {
			throw new StoreException(e);
		}
	}

}
