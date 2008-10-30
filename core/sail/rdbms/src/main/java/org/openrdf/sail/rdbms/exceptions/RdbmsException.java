/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.exceptions;

import java.sql.BatchUpdateException;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.store.StoreException;

/**
 * SailExcetion from an RDBMS store.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsException extends StoreException {
	private static Logger logger = LoggerFactory.getLogger(RdbmsException.class);

	private static final long serialVersionUID = -4004800841908629772L;

	private static SQLException findInterestingCause(SQLException e) {
		if (e instanceof BatchUpdateException) {
			BatchUpdateException b = (BatchUpdateException) e;
			logger.error(b.toString(), b);
			return b.getNextException();
		}
		return e;
	}

	public RdbmsException(SQLException e) {
		super(findInterestingCause(e));
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
