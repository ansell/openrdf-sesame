/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2002-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.NameValuePair;

import org.openrdf.http.client.connections.HTTPConnection;
import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.exceptions.HTTPException;
import org.openrdf.http.protocol.exceptions.Unauthorized;
import org.openrdf.http.protocol.exceptions.UnsupportedFileFormat;
import org.openrdf.http.protocol.exceptions.UnsupportedMediaType;
import org.openrdf.http.protocol.exceptions.UnsupportedQueryLanguage;
import org.openrdf.model.Resource;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.store.StoreException;

/**
 * @author Herko ter Horst
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class SizeClient {

	private HTTPConnectionPool size;

	public SizeClient(HTTPConnectionPool size) {
		this.size = size;
	}

	/*-------------------------*
	 * Repository/context size *
	 *-------------------------*/

	public long get(Resource... contexts)
		throws StoreException
	{
		HTTPConnection method = size.get();

		String[] encodedContexts = Protocol.encodeContexts(contexts);

		List<NameValuePair> contextParams = new ArrayList<NameValuePair>(encodedContexts.length);
		for (int i = 0; i < encodedContexts.length; i++) {
			contextParams.add(new NameValuePair(Protocol.CONTEXT_PARAM_NAME, encodedContexts[i]));
		}
		method.sendQueryString(contextParams);

		try {
			method.acceptString();
			execute(method);
			return method.readLong();
		}
		catch (NumberFormatException e) {
			throw new StoreException("Server responded with invalid size value");
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
