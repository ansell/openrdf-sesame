/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.exceptions;

import java.net.HttpURLConnection;

/**
 * The server does not support the functionality required to fulfill the
 * request.
 * 
 * @author James Leigh
 */
public class NotImplemented extends ServerHTTPException {

	private static final long serialVersionUID = -4319165421342407785L;

	private static final int STATUS_CODE = HttpURLConnection.HTTP_NOT_IMPLEMENTED;

	public NotImplemented(String msg) {
		super(STATUS_CODE, msg);
	}

}
