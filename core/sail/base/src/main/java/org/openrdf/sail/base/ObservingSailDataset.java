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

import org.openrdf.IsolationLevels;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.SailException;

/**
 * A {@link IsolationLevels#SERIALIZABLE} {@link SailDataset} that tracks the
 * observed statement patterns to an
 * {@link SailSink#observe(Resource, URI, Value, Resource...)} to check
 * consistency.
 * 
 * @author James Leigh
 */
class ObservingSailDataset extends DelegatingSailDataset {

	/**
	 * The {@link SailSink} that is tracking the statement patterns.
	 */
	private final SailSink observer;

	/**
	 * Creates a {@link IsolationLevels#SERIALIZABLE} {@link SailDataset} that
	 * tracks consistency.
	 * 
	 * @param delegate
	 *        to be {@link SailDataset#close()} when this {@link SailDataset} is
	 *        closed.
	 * @param observer
	 *        to be {@link SailSink#flush()} and {@link SailSink#close()} when this
	 *        {@link SailDataset} is closed.
	 */
	public ObservingSailDataset(SailDataset delegate, SailSink observer) {
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
	public SailIteration<? extends Resource> getContextIDs()
		throws SailException
	{
		observer.observe(null, null, null);
		return super.getContextIDs();
	}

	@Override
	public SailIteration<? extends Statement> get(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		observer.observe(subj, pred, obj, contexts);
		return super.get(subj, pred, obj, contexts);
	}

}
