/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.exceptions;

import org.openrdf.query.QueryEvaluationException;

/**
 * Indicates that an operation is not supported on the database side and must be
 * evaluated on the client.
 * 
 * @author James Leigh
 * 
 */
public class UnsupportedRdbmsOperatorException extends QueryEvaluationException {

	private static final long serialVersionUID = 2135660777365106900L;

	public UnsupportedRdbmsOperatorException(String string) {
		super(string);
	}

}
