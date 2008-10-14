/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import org.openrdf.query.EvaluationException;

/**
 * @author Herko ter Horst
 */
public class HTTPQueryEvaluationException extends EvaluationException {

	private static final long serialVersionUID = -8315025167877093272L;

	public HTTPQueryEvaluationException(String msg) {
		super(msg);
	}

	public HTTPQueryEvaluationException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public HTTPQueryEvaluationException(Throwable cause) {
		super(cause);
	}
}
