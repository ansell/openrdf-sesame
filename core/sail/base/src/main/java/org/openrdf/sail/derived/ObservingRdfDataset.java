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

import org.openrdf.IsolationLevels;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.SailException;

/**
 * A {@link IsolationLevels#SERIALIZABLE} {@link RdfDataset} that tracks the
 * observed statement patterns to an
 * {@link RdfSink#observe(Resource, URI, Value, Resource...)} to check
 * consistency.
 * 
 * @author James Leigh
 */
class ObservingRdfDataset extends DelegatingRdfDataset {

	/**
	 * The {@link RdfSink} that is tracking the statement patterns.
	 */
	private final RdfSink observer;

	/**
	 * Creates a {@link IsolationLevels#SERIALIZABLE} {@link RdfDataset} that
	 * tracks consistency.
	 * 
	 * @param delegate
	 *        to be {@link RdfDataset#close()} when this {@link RdfDataset} is
	 *        closed.
	 * @param observer
	 *        to be {@link RdfSink#flush()} and {@link RdfSink#close()} when this
	 *        {@link RdfDataset} is closed.
	 */
	public ObservingRdfDataset(RdfDataset delegate, RdfSink observer) {
		super(delegate);
		this.observer = observer;
	}

	@Override
	public void close()
		throws SailException
	{
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
	public RdfIteration<? extends Statement> get(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		observer.observe(subj, pred, obj, contexts);
		return super.get(subj, pred, obj, contexts);
	}

}
