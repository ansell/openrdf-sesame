/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.exceptions;

import org.openrdf.http.protocol.error.ErrorInfo;
import org.openrdf.http.protocol.error.ErrorType;


/**
 *
 * @author James Leigh
 */
public class UnsupportedQueryLanguage extends BadRequest {

	private static final long serialVersionUID = -4486188821642629533L;

	public UnsupportedQueryLanguage(String msg) {
		super(new ErrorInfo(ErrorType.UNSUPPORTED_QUERY_LANGUAGE, msg).toString());
	}

}
