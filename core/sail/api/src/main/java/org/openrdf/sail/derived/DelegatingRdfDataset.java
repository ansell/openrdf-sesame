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

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.SailException;

/**
 *
 * @author James Leigh
 */
public class DelegatingRdfDataset implements RdfDataset {
	private final RdfDataset delegate;
	private final boolean releasing;
	private boolean released;

	/**
	 * @param delegate
	 * @param releasing if {@link #release()} should be delegated
	 */
	public DelegatingRdfDataset(RdfDataset delegate, boolean releasing) {
		super();
		this.delegate = delegate;
		this.releasing = releasing;
	}

	public boolean isActive() {
		return !released && delegate.isActive();
	}

	public void release() {
		released = true;
		if (releasing) {
			delegate.release();
		}
	}

	public CloseableIteration<? extends Namespace, SailException> getNamespaces()
		throws SailException
	{
		return delegate.getNamespaces();
	}

	public String getNamespace(String prefix)
		throws SailException
	{
		return delegate.getNamespace(prefix);
	}

	public CloseableIteration<? extends Resource, SailException> getContextIDs()
		throws SailException
	{
		return delegate.getContextIDs();
	}

	public CloseableIteration<? extends Statement, SailException> getExplicit(Resource subj, URI pred,
			Value obj, Resource... contexts)
		throws SailException
	{
		return delegate.getExplicit(subj, pred, obj, contexts);
	}

	public CloseableIteration<? extends Statement, SailException> getInferred(Resource subj, URI pred,
			Value obj, Resource... contexts)
		throws SailException
	{
		return delegate.getInferred(subj, pred, obj, contexts);
	}

	public CloseableIteration<? extends Statement, SailException> getStatements(Resource subj, URI pred,
			Value obj, Resource... contexts)
		throws SailException
	{
		return delegate.getStatements(subj, pred, obj, contexts);
	}
}
