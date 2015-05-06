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

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.SailException;

/**
 *
 * @author James Leigh
 */
class ObservingRdfDataset extends DelegatingRdfDataset {
	private final RdfSink observer;

	public ObservingRdfDataset(RdfDataset delegate, RdfSink observer) {
		super(delegate, true);
		this.observer = observer;
	}

	@Override
	public void close() throws SailException {
		super.close();
		// flush observer regardless of consistency
		observer.flush();
		observer.close();
	}

	@Override
	public RdfIteration<? extends Resource> getContextIDs()
		throws SailException
	{
		observer.observe(null, null, null);
		return super.getContextIDs();
	}

	@Override
	public RdfIteration<? extends Statement> get(Resource subj, URI pred,
			Value obj, Resource... contexts)
		throws SailException
	{
		observer.observe(subj, pred, obj, contexts);
		return super.get(subj, pred, obj, contexts);
	}
	
}
