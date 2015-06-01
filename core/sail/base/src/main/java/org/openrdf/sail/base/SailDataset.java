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
package org.openrdf.sail.base;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.IsolationLevels;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
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
	 *        A URI specifying the predicate, or <tt>null</tt> for a wildcard.
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
	CloseableIteration<? extends Statement, SailException> getStatements(Resource subj, URI pred, Value obj,
			Resource... contexts)
		throws SailException;

}
