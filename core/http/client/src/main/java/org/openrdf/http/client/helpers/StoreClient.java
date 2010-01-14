/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2010.
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
import org.openrdf.result.TupleResult;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class StoreClient {

	private final HTTPConnectionPool pool;

	private volatile String match;

	private volatile String eTag;

	private volatile int maxAge;

	public StoreClient(HTTPConnectionPool pool) {
		this.pool = pool;
	}

	public int getMaxAge() {
		return maxAge;
	}

	public String getETag() {
		return eTag;
	}

	public void ifNoneMatch(String eTag) {
		match = eTag;
	}

	public TupleResult list()
		throws StoreException
	{
		HTTPConnection con = pool.get();

		try {
			con.acceptTupleQueryResult();
			execute(con);
			if (con.isNotModified()) {
				return null;
			}
			return con.getTupleQueryResult();
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
		HTTPConnection con = pool.post();
		try {
			execute(con);
			if (con.isNotModified()) {
				return null;
			}
			return con.readLocation();
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			con.release();
		}
	}

	public <T> T get(Class<T> type)
		throws StoreException
	{
		HTTPConnection con = pool.get();

		try {
			con.accept(type);
			execute(con);
			if (con.isNotModified()) {
				return null;
			}
			return con.read(type);
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
			con.release();
		}
	}

	public void post()
		throws StoreException
	{
		HTTPConnection con = pool.post();
		try {
			execute(con);
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			con.release();
		}
	}

	public void put(Object instance)
		throws StoreException
	{
		HTTPConnection con = pool.put();
		try {
			con.send(instance);
			execute(con);
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			con.release();
		}
	}

	public void post(Object instance)
		throws StoreException
	{
		HTTPConnection con = pool.post();
		try {
			con.send(instance);
			execute(con);
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			con.release();
		}
	}

	public void delete()
		throws StoreException
	{
		HTTPConnection con = pool.delete();
		try {
			execute(con);
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			con.release();
		}
	}

	public <T> T get(String id, Class<T> type)
		throws StoreException
	{
		HTTPConnection con = pool.slash(id).get();

		try {
			con.accept(type);
			try {
				con.execute();
				if (con.isNotModified()) {
					return null;
				}
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
			return con.read(type);
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
			con.release();
		}
	}

	public void put(String id, Object instance)
		throws StoreException
	{
		HTTPConnection con = pool.slash(id).put();
		try {
			con.send(instance);
			execute(con);
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			con.release();
		}
	}

	public void delete(String id)
		throws StoreException
	{
		HTTPConnection con = pool.slash(id).delete();
		try {
			execute(con);
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			con.release();
		}
	}

	private void execute(HTTPConnection con)
		throws IOException, StoreException
	{
		try {
			if (match != null) {
				con.ifNoneMatch(match);
				match = null;
			}
			con.execute();
			eTag = con.readETag();
			maxAge = con.readMaxAge();
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
