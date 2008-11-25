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
public class MalformedQuery extends BadRequest {

	private static final long serialVersionUID = -6836412684686503188L;

	public MalformedQuery(String msg) {
		super(new ErrorInfo(ErrorType.MALFORMED_QUERY, msg).toString());
	}

}
