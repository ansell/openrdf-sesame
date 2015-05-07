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

import org.openrdf.IsolationLevel;
import org.openrdf.sail.SailException;

/**
 * Allows an {@link RdfSource} to be used as an {@link RdfBranch}. Calls to
 * {@link #close()}, {@link #prepare()}, and {@link #flush()} are ignored.
 * 
 * @author James Leigh
 */
public class RdfNotBranchedSource implements RdfBranch {

	/**
	 * Target {@link RdfSource} for calls to this {@link RdfBranch}.
	 */
	private final RdfSource source;

	/**
	 * Wraps an {@link RdfSource} with the {@link RdfBranch} interface. All
	 * applicable calls are delegated, others are ignored.
	 * 
	 * @param source
	 */
	public RdfNotBranchedSource(RdfSource source) {
		this.source = source;
	}

	public RdfBranch fork() {
		return source.fork();
	}

	public RdfSink sink(IsolationLevel level)
		throws SailException
	{
		return source.sink(level);
	}

	public RdfDataset dataset(IsolationLevel level)
		throws SailException
	{
		return source.dataset(level);
	}

	@Override
	public void close()
		throws SailException
	{
		// no-op
	}

	@Override
	public void prepare()
		throws SailException
	{
		// no-op
	}

	@Override
	public void flush()
		throws SailException
	{
		// no-op
	}
}
