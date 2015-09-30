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
import org.openrdf.sail.SailException;

/**
 * A wrapper around an {@link SailSource} that can suppress the call to
 * {@link #close()}. This is useful when the a shared branch is sometimes to be
 * used and other times a dedicated branch is to be used.
 * 
 * @author James Leigh
 */
class DelegatingSailSource implements SailSource {

	private final SailSource delegate;

	private final boolean releasing;

	/**
	 * Wraps this {@link SailSource}, delegating all calls to it unless
	 * <code>closing</code> is false, in which case {@link #close()} will not be
	 * delegated.
	 * 
	 * @param delegate
	 * @param closing
	 *        if {@link #close()} should be delegated
	 */
	public DelegatingSailSource(SailSource delegate, boolean closing) {
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

	public SailSource fork() {
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
