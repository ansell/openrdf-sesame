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
package org.openrdf.sail.derived;

import org.openrdf.sail.SailConflictException;
import org.openrdf.sail.SailException;

/**
 * A persistent yet mutable source or container of RDF graphs. In which its
 * state can change over time. The life cycle follows that of a transaction.
 * 
 * @author James Leigh
 */
public interface RdfBranch extends RdfSource, RdfClosable {

	/**
	 * Check the consistency of this {@link RdfBranch} and throw a
	 * {@link SailConflictException} if {@link RdfBranch#flush()}ing this
	 * {@link RdfBranch} would cause the backing {@link RdfSource} to be
	 * inconsistent.
	 * 
	 * @throws SailException
	 */
	void prepare()
		throws SailException;

	/**
	 * Apply all the changes to this {@link RdfBranch} to the backing
	 * {@link RdfSource}.
	 * 
	 * @throws SailException
	 */
	void flush()
		throws SailException;

}
