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

import org.openrdf.IsolationLevel;
import org.openrdf.IsolationLevels;
import org.openrdf.sail.SailConflictException;
import org.openrdf.sail.SailException;

/**
 * A persistent yet mutable source or container of RDF graphs. In which its
 * state can change over time. The life cycle follows that of a store and/or
 * transactions. The {@link SailClosable#close()} is only applicable to results
 * from {@link #fork()}, not to the backing {@link SailSource} itself.
 * 
 * @author James Leigh
 */
public interface SailSource extends SailClosable {

	/**
	 * Creates a new branch of this source. When it's {@link #flush()} is called
	 * the changes are applied to this backing source.
	 * 
	 * @return a branched {@link SailSource}.
	 */
	SailSource fork();

	/**
	 * Create a {@link SailSink} that when when its {@link #flush()} is called,
	 * the changes are applied to this source.
	 * 
	 * @param level
	 *        If this level is compatible with
	 *        {@link IsolationLevels#SERIALIZABLE} then a
	 *        {@link SailSink#prepare()} can throw a
	 *        {@link SailConflictException}.
	 * @return Newly created {@link SailSink}
	 * @throws SailException
	 */
	SailSink sink(IsolationLevel level)
		throws SailException;

	/**
	 * Create an observable {@link SailDataset} of the current state of this
	 * {@link SailSource}. Repeatedly calling with methods with
	 * {@link IsolationLevels#SNAPSHOT} (or higher) isolation levels will result
	 * in {@link SailDataset}s that are all derived from the same state of the
	 * backing {@link SailSource} (if applicable), that is the only difference
	 * between the states of the {@link SailDataset} will be from changes using
	 * this {@link #sink(IsolationLevel)}.
	 * 
	 * @param level
	 *        If this is compatible with {@link IsolationLevels#SNAPSHOT_READ}
	 *        the resulting {@link SailDataset} will observe a single state of
	 *        this {@link SailSource}.
	 * @return an {@link SailDataset} of the current state
	 * @throws SailException
	 */
	SailDataset dataset(IsolationLevel level)
		throws SailException;

	/**
	 * Check the consistency of this branch and throws a
	 * {@link SailConflictException} if {@link #flush()}ing this branch would
	 * cause the backing {@link SailSource} to be inconsistent, if applicable. If
	 * this is the final backing {@link SailSource} calling this method has no
	 * effect.
	 * 
	 * @throws SailException
	 */
	void prepare()
		throws SailException;

	/**
	 * Apply all the changes to this branch to the backing {@link SailSource}, if
	 * applicable. If this is the final backing {@link SailSource} calling this
	 * method has no effect.
	 * 
	 * @throws SailException
	 */
	void flush()
		throws SailException;

}
