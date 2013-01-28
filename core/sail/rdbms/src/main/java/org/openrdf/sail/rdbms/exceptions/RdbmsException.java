/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.sail.rdbms.exceptions;

import java.sql.BatchUpdateException;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.sail.SailException;

/**
 * SailExcetion from an RDBMS store.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsException extends SailException {
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
