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

import java.sql.SQLException;

/**
 * Thrown when no exception is declared.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 9001754124114839089L;

	public RdbmsRuntimeException(SQLException e) {
		super(e);
	}

	public RdbmsRuntimeException(String msg) {
		super(msg);
	}

	public RdbmsRuntimeException(RdbmsException e) {
		super(e);
	}

	public RdbmsRuntimeException(InterruptedException e) {
		super(e);
	}

}
