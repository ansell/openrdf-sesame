/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.exceptions;

import org.openrdf.http.protocol.error.ErrorInfo;
import org.openrdf.http.protocol.error.ErrorType;

/**
 * @author James Leigh
 */
public class UnsupportedFileFormat extends BadRequest {

	private static final long serialVersionUID = 2224008763380338386L;

	public UnsupportedFileFormat(String msg) {
		super(new ErrorInfo(ErrorType.UNSUPPORTED_FILE_FORMAT, msg).toString());
	}

}
