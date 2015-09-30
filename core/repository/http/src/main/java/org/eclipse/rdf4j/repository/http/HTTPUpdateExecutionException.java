/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.repository.http;

import java.io.IOException;

import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.repository.RepositoryException;


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
