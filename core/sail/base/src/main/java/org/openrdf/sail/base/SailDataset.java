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
package org.openrdf.sail.base;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.IsolationLevels;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.sail.SailException;

/**
 * A state of an {@link SailSource} at a point in time that will remain
 * consistent until {@link #close()} is called. The life cycle follows that of a
 * read operation.
 * 
 * @author James Leigh
 */
public interface SailDataset extends SailClosable {

	/**
	 * Called when this {@link SailDataset} is no longer is used, such as when a
	 * read operation is complete. An isolation level compatible with
	 * {@link IsolationLevels#SNAPSHOT} will ensure the state of this
	 * {@link SailDataset} dose not change between the first call to this object
	 * until {@link #release()} is called.
	 */
	void close()
		throws SailException;

	/**
	 * Gets the namespaces relevant to the data contained in this object.
	 * 
	 * @return An iterator over the relevant namespaces, should not contain any
	 *         duplicates.
	 * @throws SailException
	 *         If this object encountered an error or unexpected situation
	 *         internally.
	 */
	CloseableIteration<? extends Namespace, SailException> getNamespaces()
		throws SailException;

	/**
	 * Gets the namespace that is associated with the specified prefix, if any.
	 * 
	 * @param prefix
	 *        A namespace prefix, or an empty string in case of the default
	 *        namespace.
	 * @return The namespace name that is associated with the specified prefix,
	 *         or <tt>null</tt> if there is no such namespace.
	 * @throws SailException
	 *         If this object encountered an error or unexpected situation
	 *         internally.
	 * @throws NullPointerException
	 *         In case <tt>prefix</tt> is <tt>null</tt>.
	 */
	String getNamespace(String prefix)
		throws SailException;

	/**
	 * Returns the set of all unique context identifiers that are used to store
	 * statements.
	 * 
	 * @return An iterator over the context identifiers, should not contain any
	 *         duplicates.
	 */
	CloseableIteration<? extends Resource, SailException> getContextIDs()
		throws SailException;

	/**
	 * Gets all statements that have a specific subject, predicate and/or object.
	 * All three parameters may be null to indicate wildcards. Optionally a (set
	 * of) context(s) may be specified in which case the result will be
	 * restricted to statements matching one or more of the specified contexts.
	 * 
	 * @param subj
	 *        A Resource specifying the subject, or <tt>null</tt> for a wildcard.
	 * @param pred
	 *        A IRI specifying the predicate, or <tt>null</tt> for a wildcard.
	 * @param obj
	 *        A Value specifying the object, or <tt>null</tt> for a wildcard.
	 * @param contexts
	 *        The context(s) to get the statements from. Note that this parameter
	 *        is a vararg and as such is optional. If no contexts are supplied
	 *        the method operates on all contexts.
	 * @return An iterator over the relevant statements.
	 * @throws SailException
	 *         If the triple source failed to get the statements.
	 */
	CloseableIteration<? extends Statement, SailException> getStatements(Resource subj, IRI pred, Value obj,
			Resource... contexts)
		throws SailException;

}
