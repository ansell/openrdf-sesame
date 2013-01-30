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
package org.openrdf.repository.http;

import java.io.IOException;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryException;


/**
 *
 * @author Jeen Broekstra
 */
public class HTTPUpdateExecutionException extends UpdateExecutionException {

	private static final long serialVersionUID = -8315025167877093273L;

	/**
	 * @param msg
	 */
	public HTTPUpdateExecutionException(String msg) {
		super(msg);
	}
	/**
	 * @param msg
	 * @param cause
	 */
	public HTTPUpdateExecutionException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * @param cause
	 */
	public HTTPUpdateExecutionException(Throwable cause) {
		super(cause);
	}

	public boolean isCausedByIOException() {
		return getCause() instanceof IOException;
	}
	
	public boolean isCausedByRepositoryException() {
		return getCause() instanceof RepositoryException;
	}
	
	public boolean isCausedByMalformedQueryException() {
		return getCause() instanceof MalformedQueryException;
	}

	public IOException getCauseAsIOException() {
		return (IOException)getCause();
	}

	public RepositoryException getCauseAsRepositoryException() {
		return (RepositoryException)getCause();
	}

	public MalformedQueryException getCauseAsMalformedQueryException() {
		return (MalformedQueryException)getCause();
	}
}
