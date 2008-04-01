/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.exceptions;

import java.sql.SQLException;

import org.openrdf.sail.SailException;

/**
 * SailExcetion from an RDBMS store.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsException extends SailException {

	private static final long serialVersionUID = -4004800841908629772L;

	public RdbmsException(SQLException e) {
		super(e);
	}

	public RdbmsException(String msg) {
		super(msg);
	}

	public RdbmsException(String msg, Exception e) {
		super(msg, e);
	}

	public RdbmsException(Exception e) {
		super(e);
	}

}
