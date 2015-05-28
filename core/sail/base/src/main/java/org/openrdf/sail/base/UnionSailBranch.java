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
 * Combines two branches to act as a single {@link SailBranch}. This is useful to
 * provide a combined view of both explicit and inferred statements.
 * 
 * @author James Leigh
 */
class UnionSailBranch implements SailBranch {

	/**
	 * The branch that will be used in calls to {@link #sink(IsolationLevel)}.
	 */
	private final SailBranch primary;

	/**
	 * Additional statements that should be included in {@link SailDataset}s.
	 */
	private final SailBranch additional;

	/**
	 * An {@link SailBranch} that combines two other {@link SailBranch}es.
	 * 
	 * @param primary delegates all calls to the given {@link SailBranch}.
	 * @param additional delegate all call except {@link #sink(IsolationLevel)}.
	 */
	public UnionSailBranch(SailBranch primary, SailBranch additional) {
		super();
		this.primary = primary;
		this.additional = additional;
	}

	public String toString() {
		return primary.toString() + "\n" + additional.toString();
	}

	@Override
	public void close()
		throws SailException
	{
		primary.close();
		additional.close();
	}

	@Override
	public SailBranch fork() {
		return new UnionSailBranch(primary.fork(), additional.fork());
	}

	public void prepare()
		throws SailException
	{
		primary.prepare();
		additional.prepare();
	}

	public void flush()
		throws SailException
	{
		primary.flush();
		additional.flush();
	}

	@Override
	public SailSink sink(IsolationLevel level)
		throws SailException
	{
		return primary.sink(level);
	}

	@Override
	public SailDataset dataset(IsolationLevel level)
		throws SailException
	{
		return new UnionSailDataset(primary.dataset(level), additional.dataset(level));
	}

}
