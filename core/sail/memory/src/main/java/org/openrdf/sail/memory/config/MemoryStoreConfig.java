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
package org.openrdf.sail.memory.config;

import static org.openrdf.sail.memory.config.MemoryStoreSchema.PERSIST;
import static org.openrdf.sail.memory.config.MemoryStoreSchema.SYNC_DELAY;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailImplConfigBase;

/**
 * @author Arjohn Kampman
 */
public class MemoryStoreConfig extends SailImplConfigBase {

	private boolean persist = false;

	private long syncDelay = 0L;

	public MemoryStoreConfig() {
		super(MemoryStoreFactory.SAIL_TYPE);
	}

	public MemoryStoreConfig(boolean persist) {
		this();
		setPersist(persist);
	}

	public MemoryStoreConfig(boolean persist, long syncDelay) {
		this(persist);
		setSyncDelay(syncDelay);
	}

	public boolean getPersist() {
		return persist;
	}

	public void setPersist(boolean persist) {
		this.persist = persist;
	}

	public long getSyncDelay() {
		return syncDelay;
	}

	public void setSyncDelay(long syncDelay) {
		this.syncDelay = syncDelay;
	}

	@Override
	public Resource export(Graph graph)
	{
		Resource implNode = super.export(graph);

		if (persist) {
			graph.add(implNode, PERSIST, graph.getValueFactory().createLiteral(persist));
		}

		if (syncDelay != 0) {
			graph.add(implNode, SYNC_DELAY, graph.getValueFactory().createLiteral(syncDelay));
		}

		return implNode;
	}

	@Override
	public void parse(Graph graph, Resource implNode)
		throws SailConfigException
	{
		super.parse(graph, implNode);

		try {
			Literal persistValue = GraphUtil.getOptionalObjectLiteral(graph, implNode, PERSIST);
			if (persistValue != null) {
				try {
					setPersist((persistValue).booleanValue());
				}
				catch (IllegalArgumentException e) {
					throw new SailConfigException("Boolean value required for " + PERSIST + " property, found "
							+ persistValue);
				}
			}

			Literal syncDelayValue = GraphUtil.getOptionalObjectLiteral(graph, implNode, SYNC_DELAY);
			if (syncDelayValue != null) {
				try {
					setSyncDelay((syncDelayValue).longValue());
				}
				catch (NumberFormatException e) {
					throw new SailConfigException("Long integer value required for " + SYNC_DELAY
							+ " property, found " + syncDelayValue);
				}
			}
		}
		catch (GraphUtilException e) {
			throw new SailConfigException(e.getMessage(), e);
		}
	}
}
