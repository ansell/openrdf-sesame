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
public class MalformedData extends BadRequest {

	private static final long serialVersionUID = -6138770791457273654L;

	public MalformedData(String msg) {
		super(new ErrorInfo(ErrorType.MALFORMED_DATA, msg).toString());
	}

}
