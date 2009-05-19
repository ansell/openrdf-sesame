/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import org.openrdf.http.protocol.error.ErrorInfo;
import org.openrdf.http.protocol.error.ErrorType;

/**
 * @author Arjohn Kampman
 */
public class ErrorInfoException extends RuntimeException {

	private static final long serialVersionUID = 8056400029162650973L;

	private final ErrorInfo errInfo;

	public ErrorInfoException(ErrorType errType, String errMsg) {
		this(new ErrorInfo(errType, errMsg));
	}

	public ErrorInfoException(ErrorInfo errInfo) {
		super(errInfo.toString());
		this.errInfo = errInfo;
	}

	public ErrorInfo getErrorInfo() {
		return errInfo;
	}
}
