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
package org.openrdf.http.protocol.transaction.operations;

import java.util.Arrays;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.Resource;

/**
 * A TransactionOperation that operates on a specific (set of) contexts.
 * 
 * @author Arjohn Kampman
 * @author Leo Sauermann
 */
public abstract class ContextOperation implements TransactionOperation {

	protected Resource[] contexts;

	protected ContextOperation(Resource... contexts) {
		setContexts(contexts);
	}

	public Resource[] getContexts() {
		return contexts;
	}

	public void setContexts(Resource... contexts) {
		OpenRDFUtil.verifyContextNotNull(contexts);

		this.contexts = contexts;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof ContextOperation) {
			ContextOperation o = (ContextOperation)other;
			return Arrays.deepEquals(getContexts(), o.getContexts());
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		return Arrays.deepHashCode(getContexts());
	}
}
