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
 * @author James Leigh
 */
public class UnionRdfSource implements RdfSource {

	private final RdfSource primary;

	private final RdfSource additional;

	/**
	 * @param sources
	 */
	public UnionRdfSource(RdfSource primary, RdfSource additional) {
		super();
		this.primary = primary;
		this.additional = additional;
	}

	public String toString() {
		return primary.toString();
	}

	@Override
	public void close() throws SailException {
		primary.close();
		additional.close();
	}

	@Override
	public RdfSource fork() {
		return new UnionRdfSource(primary.fork(), additional.fork());
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
	public RdfSink sink(IsolationLevel level)
		throws SailException
	{
		return primary.sink(level);
	}

	@Override
	public RdfDataset snapshot(IsolationLevel level)
		throws SailException
	{
		return new UnionRdfDataset(primary.snapshot(level), additional.snapshot(level));
	}

}
