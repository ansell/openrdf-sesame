/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
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
					setPersist(((Literal)persistValue).booleanValue());
				}
				catch (IllegalArgumentException e) {
					throw new SailConfigException("Boolean value required for " + PERSIST + " property, found "
							+ persistValue);
				}
			}

			Literal syncDelayValue = GraphUtil.getOptionalObjectLiteral(graph, implNode, SYNC_DELAY);
			if (syncDelayValue != null) {
				try {
					setSyncDelay(((Literal)syncDelayValue).longValue());
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
