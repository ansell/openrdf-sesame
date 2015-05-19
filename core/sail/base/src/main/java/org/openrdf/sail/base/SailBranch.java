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

import org.openrdf.sail.SailConflictException;
import org.openrdf.sail.SailException;

/**
 * A persistent yet mutable source or container of RDF graphs. In which its
 * state can change over time. The life cycle follows that of a transaction.
 * 
 * @author James Leigh
 */
public interface SailBranch extends SailSource, SailClosable {

	/**
	 * Check the consistency of this {@link SailBranch} and throw a
	 * {@link SailConflictException} if {@link SailBranch#flush()}ing this
	 * {@link SailBranch} would cause the backing {@link SailSource} to be
	 * inconsistent.
	 * 
	 * @throws SailException
	 */
	void prepare()
		throws SailException;

	/**
	 * Apply all the changes to this {@link SailBranch} to the backing
	 * {@link SailSource}.
	 * 
	 * @throws SailException
	 */
	void flush()
		throws SailException;

}
