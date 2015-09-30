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
