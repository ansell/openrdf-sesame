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
package org.openrdf.query;

import org.openrdf.OpenRDFException;

/**
 * An exception indicating that the execution of an update failed.
 * 
 * @author Jeen
 */
public class UpdateExecutionException extends OpenRDFException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7969399526232927434L;

	public UpdateExecutionException() {
		super();
	}

	/**
	 * Creates a new UpdateExecutionException.
	 * 
	 * @param msg
	 *        An error message.
	 */
	public UpdateExecutionException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new {@link UpdateExecutionException} wrapping another exception.
	 * 
	 * @param cause
	 *        the cause of the exception
	 */
	public UpdateExecutionException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new {@link UpdateExecutionException} wrapping another exception.
	 * 
	 * @param msg
	 *        and error message.
	 * @param cause
	 *        the cause of the exception
	 */
	public UpdateExecutionException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
