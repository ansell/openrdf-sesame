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
import org.openrdf.sail.SailException;

/**
 * Allows an {@link SailSource} to be used as an {@link SailBranch}. Calls to
 * {@link #close()}, {@link #prepare()}, and {@link #flush()} are ignored.
 * 
 * @author James Leigh
 */
class SailNotBranchedSource implements SailBranch {

	/**
	 * Target {@link SailSource} for calls to this {@link SailBranch}.
	 */
	private final SailSource source;

	/**
	 * Wraps an {@link SailSource} with the {@link SailBranch} interface. All
	 * applicable calls are delegated, others are ignored.
	 * 
	 * @param source
	 */
	public SailNotBranchedSource(SailSource source) {
		this.source = source;
	}

	public SailBranch fork() {
		return source.fork();
	}

	public SailSink sink(IsolationLevel level)
		throws SailException
	{
		return source.sink(level);
	}

	public SailDataset dataset(IsolationLevel level)
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
