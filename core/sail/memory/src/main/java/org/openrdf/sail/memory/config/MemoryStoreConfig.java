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
