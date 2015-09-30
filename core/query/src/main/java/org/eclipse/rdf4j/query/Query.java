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
package org.eclipse.rdf4j.query;

/**
 * A query on a repository that can be formulated in one of the supported query
 * languages (for example SeRQL or SPARQL). It allows one to predefine bindings
 * in the query to be able to reuse the same query with different bindings.
 * 
 * @author Arjohn Kampman
 * @author jeen
 */
public interface Query extends Operation {

	/**
	 * Specifies the maximum time that a query is allowed to run. The query will
	 * be interrupted when it exceeds the time limit. Any consecutive requests to
	 * fetch query results will result in {@link QueryInterruptedException}s.
	 * 
	 * @param maxQueryTime
	 *        The maximum query time, measured in seconds. A negative or zero
	 *        value indicates an unlimited query time (which is the default).
	 * @deprecated since 2.8.0. Use {@link Operation#setMaxExecutionTime(int)}
	 *             instead.
	 */
	@Deprecated
	public void setMaxQueryTime(int maxQueryTime);

	/**
	 * Returns the maximum query evaluation time.
	 * 
	 * @return The maximum query evaluation time, measured in seconds.
	 * @see #setMaxQueryTime(int)
	 * @deprecated since 2.8.0. Use {@link Operation#getMaxExecutionTime()}
	 *             instead.
	 */
	@Deprecated
	public int getMaxQueryTime();
}
