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
public class DelegatingRdfBranch implements RdfBranch {

	private final RdfBranch delegate;

	private final boolean releasing;

	/**
	 * @param delegate
	 * @param closing
	 *        if {@link #close()} should be delegated
	 */
	public DelegatingRdfBranch(RdfBranch delegate, boolean closing) {
		assert delegate != null;
		this.delegate = delegate;
		this.releasing = closing;
	}

	public String toString() {
		return delegate.toString();
	}

	public void close() throws SailException {
		if (releasing) {
			delegate.close();
		}
	}

	public RdfBranch fork()
	{
		return delegate.fork();
	}

	public void prepare()
		throws SailException
	{
		delegate.prepare();
	}

	public void flush()
		throws SailException
	{
		delegate.flush();
	}

	public RdfSink sink(IsolationLevel level)
		throws SailException
	{
		return delegate.sink(level);
	}

	public RdfDataset dataset(IsolationLevel level)
		throws SailException
	{
		return delegate.dataset(level);
	}
}
