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

import org.openrdf.model.Value;

/**
 * An operation (e.g. a query or an update) on a repository that can be
 * formulated in one of the supported query languages (for example SeRQL or
 * SPARQL). It allows one to predefine bindings in the operation to be able to
 * reuse the same operation with different bindings.
 * 
 * @author Jeen
 */
public interface Operation {

	/**
	 * Binds the specified variable to the supplied value. Any value that was
	 * previously bound to the specified value will be overwritten.
	 * 
	 * @param name
	 *        The name of the variable that should be bound.
	 * @param value
	 *        The (new) value for the specified variable.
	 */
	public void setBinding(String name, Value value);

	/**
	 * Removes a previously set binding on the supplied variable. Calling this
	 * method with an unbound variable name has no effect.
	 * 
	 * @param name
	 *        The name of the variable from which the binding is to be removed.
	 */
	public void removeBinding(String name);

	/**
	 * Removes all previously set bindings.
	 */
	public void clearBindings();

	/**
	 * Retrieves the bindings that have been set on this operation.
	 * 
	 * @return A (possibly empty) set of operation variable bindings.
	 * @see #setBinding(String, Value)
	 */
	public BindingSet getBindings();

	/**
	 * Specifies the dataset against which to execute an operation, overriding
	 * any dataset that is specified in the operation itself.
	 */
	public void setDataset(Dataset dataset);

	/**
	 * Gets the dataset that has been set using {@link #setDataset(Dataset)}, if
	 * any.
	 */
	public Dataset getDataset();

	/**
	 * Determine whether evaluation results of this operation should include
	 * inferred statements (if any inferred statements are present in the
	 * repository). The default setting is 'true'.
	 * 
	 * @param includeInferred
	 *        indicates whether inferred statements should be included in the
	 *        result.
	 */
	public void setIncludeInferred(boolean includeInferred);

	/**
	 * Returns whether or not this operation will return inferred statements (if
	 * any are present in the repository).
	 * 
	 * @return <tt>true</tt> if inferred statements will be returned,
	 *         <tt>false</tt> otherwise.
	 */
	public boolean getIncludeInferred();

	/**
	 * Specifies the maximum time that an operation is allowed to run. The
	 * operation will be interrupted when it exceeds the time limit. Any
	 * consecutive requests to fetch query results will result in
	 * {@link QueryInterruptedException}s or {@link UpdateInterruptedException}s
	 * (depending on whether the operation is a query or an update).
	 * 
	 * @param maxQueryTime
	 *        The maximum query time, measured in seconds. A negative or zero
	 *        value indicates an unlimited execution time (which is the default).
	 * @since 2.8.0
	 */
	public void setMaxExecutionTime(int maxExecTime);

	/**
	 * Returns the maximum operation execution time.
	 * 
	 * @return The maximum operation execution time, measured in seconds.
	 * @see #setMaxExecutionTime(int)
	 * @since 2.8.0
	 */
	public int getMaxExecutionTime();

}
