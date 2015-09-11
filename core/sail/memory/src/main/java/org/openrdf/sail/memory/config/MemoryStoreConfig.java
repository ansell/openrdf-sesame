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

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.BooleanLiteral;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.util.ModelException;
import org.openrdf.model.util.Models;
import org.openrdf.sail.config.AbstractSailImplConfig;
import org.openrdf.sail.config.SailConfigException;

/**
 * @author Arjohn Kampman
 */
public class MemoryStoreConfig extends AbstractSailImplConfig {

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
	public Resource export(Model graph) {
		Resource implNode = super.export(graph);

		if (persist) {
			graph.add(implNode, PERSIST, BooleanLiteral.TRUE);
		}

		if (syncDelay != 0) {
			graph.add(implNode, SYNC_DELAY, SimpleValueFactory.getInstance().createLiteral(syncDelay));
		}

		return implNode;
	}

	@Override
	public void parse(Model graph, Resource implNode)
		throws SailConfigException
	{
		super.parse(graph, implNode);

		try {

			Models.objectLiteral(graph.filter(implNode, PERSIST, null)).ifPresent(persistValue -> {
				try {
					setPersist((persistValue).booleanValue());
				}
				catch (IllegalArgumentException e) {
					throw new SailConfigException(
							"Boolean value required for " + PERSIST + " property, found " + persistValue);
				}
			});

			Models.objectLiteral(graph.filter(implNode, SYNC_DELAY, null)).ifPresent(syncDelayValue -> {
				try {
					setSyncDelay((syncDelayValue).longValue());
				}
				catch (NumberFormatException e) {
					throw new SailConfigException(
							"Long integer value required for " + SYNC_DELAY + " property, found " + syncDelayValue);
				}
			});
		}
		catch (ModelException e) {
			throw new SailConfigException(e.getMessage(), e);
		}
	}
}
