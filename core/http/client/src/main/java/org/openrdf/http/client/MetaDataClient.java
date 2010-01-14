/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import java.io.IOException;

import org.openrdf.http.client.connections.HTTPConnection;
import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.protocol.exceptions.HTTPException;
import org.openrdf.http.protocol.exceptions.NoCompatibleMediaType;
import org.openrdf.http.protocol.exceptions.Unauthorized;
import org.openrdf.http.protocol.exceptions.UnsupportedFileFormat;
import org.openrdf.http.protocol.exceptions.UnsupportedMediaType;
import org.openrdf.http.protocol.exceptions.UnsupportedQueryLanguage;
import org.openrdf.model.Model;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class MetaDataClient {

	private final HTTPConnectionPool pool;

	public MetaDataClient(HTTPConnectionPool pool) {
		this.pool = pool;
	}

	public Model get()
		throws StoreException
	{
		HTTPConnection con = pool.get();

		try {
			con.acceptRDF(false);
			execute(con);
			return con.readModel();
		}
		catch (NumberFormatException e) {
			throw new StoreException("Server responded with invalid size value");
		}
		catch (IOException e) {
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

	private void execute(HTTPConnection con)
		throws IOException, StoreException
	{
		try {
			con.execute();
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
			throw new StoreException(e);
		}
		catch (HTTPException e) {
			throw new StoreException(e);
		}
	}
}
