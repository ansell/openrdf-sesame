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
 * The super class of exceptions originating from {@link QueryResultHandler}
 * implementations.
 * 
 * @author Peter Ansell
 * @since 2.7.0
 */
public class QueryResultHandlerException extends OpenRDFException {

	private static final long serialVersionUID = 5096811224670124398L;

	/**
	 * Creates a new QueryResultHandlerException.
	 * 
	 * @param msg
	 *        An error message.
	 */
	public QueryResultHandlerException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new QueryResultHandlerException wrapping another exception.
	 * 
	 * @param t
	 *        The cause of the exception.
	 */
	public QueryResultHandlerException(Throwable t) {
		super(t);
	}

	/**
	 * Creates a new QueryResultHandlerException wrapping another exception.
	 * 
	 * @param msg
	 *        An error message.
	 * @param t
	 *        The cause of the exception.
	 */
	public QueryResultHandlerException(String msg, Throwable t) {
		super(msg, t);
	}

}