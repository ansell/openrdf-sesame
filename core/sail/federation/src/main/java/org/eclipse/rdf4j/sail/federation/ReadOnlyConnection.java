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
package org.eclipse.rdf4j.sail.federation;

import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.SailReadOnlyException;

/**
 * Finishes the {@link AbstractFederationConnection} by throwing
 * {@link SailReadOnlyException}s for all write operations.
 * 
 * @author James Leigh
 */
class ReadOnlyConnection extends AbstractFederationConnection {

	public ReadOnlyConnection(Federation federation, List<RepositoryConnection> members) {
		super(federation, members);
	}

	@Override
	public void setNamespaceInternal(String prefix, String name)
		throws SailException
	{
		throw new SailReadOnlyException("");
	}

	@Override
	public void clearNamespacesInternal()
		throws SailException
	{
		throw new SailReadOnlyException("");
	}

	@Override
	public void removeNamespaceInternal(String prefix)
		throws SailException
	{
		throw new SailReadOnlyException("");
	}

	@Override
	public void addStatementInternal(Resource subj, IRI pred, Value obj, Resource... contexts)
		throws SailException
	{
		throw new SailReadOnlyException("");
	}

	@Override
	public void removeStatementsInternal(Resource subj, IRI pred, Value obj, Resource... context)
		throws SailException
	{
		throw new SailReadOnlyException("");
	}

	@Override
	protected void clearInternal(Resource... contexts)
		throws SailException
	{
		throw new SailReadOnlyException("");
	}

	@Override
	protected void commitInternal()
		throws SailException
	{
		// no-op
	}

	@Override
	protected void rollbackInternal()
		throws SailException
	{
		// no-op
	}

	@Override
	protected void startTransactionInternal()
		throws SailException
	{
		// no-op
	}
}
