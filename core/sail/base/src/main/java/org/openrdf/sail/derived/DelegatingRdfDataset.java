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
class DelegatingRdfDataset implements RdfDataset {
	private final RdfDataset delegate;
	private final boolean releasing;

	/**
	 * @param delegate
	 * @param closing if {@link #close()} should be delegated
	 */
	public DelegatingRdfDataset(RdfDataset delegate, boolean closing) {
		super();
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

	public RdfIteration<? extends Namespace> getNamespaces()
		throws SailException
	{
		return delegate.getNamespaces();
	}

	public String getNamespace(String prefix)
		throws SailException
	{
		return delegate.getNamespace(prefix);
	}

	public RdfIteration<? extends Resource> getContextIDs()
		throws SailException
	{
		return delegate.getContextIDs();
	}

	public RdfIteration<? extends Statement> get(Resource subj, URI pred,
			Value obj, Resource... contexts)
		throws SailException
	{
		return delegate.get(subj, pred, obj, contexts);
	}
}
