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
 * A wrapper around an {@link SailBranch} that can suppress the call to
 * {@link #close()}. This is useful when the a shared branch is sometimes to be
 * used and other times a dedicated branch is to be used.
 * 
 * @author James Leigh
 */
public class DelegatingSailBranch implements SailBranch {

	private final SailBranch delegate;

	private final boolean releasing;

	/**
	 * Wraps this {@link SailBranch}, delegating all calls to it unless
	 * <code>closing</code> is false, in which case {@link #close()} will not be
	 * delegated.
	 * 
	 * @param delegate
	 * @param closing
	 *        if {@link #close()} should be delegated
	 */
	public DelegatingSailBranch(SailBranch delegate, boolean closing) {
		assert delegate != null;
		this.delegate = delegate;
		this.releasing = closing;
	}

	public String toString() {
		return delegate.toString();
	}

	public void close()
		throws SailException
	{
		if (releasing) {
			delegate.close();
		}
	}

	public SailBranch fork() {
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

	public SailSink sink(IsolationLevel level)
		throws SailException
	{
		return delegate.sink(level);
	}

	public SailDataset dataset(IsolationLevel level)
		throws SailException
	{
		return delegate.dataset(level);
	}
}
