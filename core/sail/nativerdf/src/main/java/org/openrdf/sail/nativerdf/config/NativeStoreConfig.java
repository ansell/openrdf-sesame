/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf.config;

import static org.openrdf.sail.nativerdf.config.NativeStoreSchema.FORCE_SYNC;
import static org.openrdf.sail.nativerdf.config.NativeStoreSchema.NAMESPACE_CACHE_SIZE;
import static org.openrdf.sail.nativerdf.config.NativeStoreSchema.NAMESPACE_ID_CACHE_SIZE;
import static org.openrdf.sail.nativerdf.config.NativeStoreSchema.TRIPLE_INDEXES;
import static org.openrdf.sail.nativerdf.config.NativeStoreSchema.VALUE_CACHE_SIZE;
import static org.openrdf.sail.nativerdf.config.NativeStoreSchema.VALUE_ID_CACHE_SIZE;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailImplConfigBase;

/**
 * @author Arjohn Kampman
 */
public class NativeStoreConfig extends SailImplConfigBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private String tripleIndexes;

	private boolean forceSync = false;

	private int valueCacheSize = -1;

	private int valueIDCacheSize = -1;

	private int namespaceCacheSize = -1;

	private int namespaceIDCacheSize = -1;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public NativeStoreConfig() {
		super(NativeStoreFactory.SAIL_TYPE);
	}

	public NativeStoreConfig(String tripleIndexes) {
		this();
		setTripleIndexes(tripleIndexes);
	}

	public NativeStoreConfig(String tripleIndexes, boolean forceSync) {
		this(tripleIndexes);
		setForceSync(forceSync);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public String getTripleIndexes() {
		return tripleIndexes;
	}

	public void setTripleIndexes(String tripleIndexes) {
		this.tripleIndexes = tripleIndexes;
	}

	public boolean getForceSync() {
		return forceSync;
	}

	public void setForceSync(boolean forceSync) {
		this.forceSync = forceSync;
	}

	public int getValueCacheSize() {
		return valueCacheSize;
	}

	public void setValueCacheSize(int valueCacheSize) {
		this.valueCacheSize = valueCacheSize;
	}

	public int getValueIDCacheSize() {
		return valueIDCacheSize;
	}

	public void setValueIDCacheSize(int valueIDCacheSize) {
		this.valueIDCacheSize = valueIDCacheSize;
	}

	public int getNamespaceCacheSize() {
		return namespaceCacheSize;
	}

	public void setNamespaceCacheSize(int namespaceCacheSize) {
		this.namespaceCacheSize = namespaceCacheSize;
	}

	public int getNamespaceIDCacheSize() {
		return namespaceIDCacheSize;
	}

	public void setNamespaceIDCacheSize(int namespaceIDCacheSize) {
		this.namespaceIDCacheSize = namespaceIDCacheSize;
	}

	@Override
	public Resource export(Graph graph) {
		Resource implNode = super.export(graph);
		ValueFactory vf = graph.getValueFactory();

		if (tripleIndexes != null) {
			graph.add(implNode, TRIPLE_INDEXES, vf.createLiteral(tripleIndexes));
		}
		if (forceSync) {
			graph.add(implNode, FORCE_SYNC, vf.createLiteral(forceSync));
		}
		if (valueCacheSize >= 0) {
			graph.add(implNode, VALUE_CACHE_SIZE, vf.createLiteral(valueCacheSize));
		}
		if (valueIDCacheSize >= 0) {
			graph.add(implNode, VALUE_ID_CACHE_SIZE, vf.createLiteral(valueIDCacheSize));
		}
		if (namespaceCacheSize >= 0) {
			graph.add(implNode, NAMESPACE_CACHE_SIZE, vf.createLiteral(namespaceCacheSize));
		}
		if (namespaceIDCacheSize >= 0) {
			graph.add(implNode, NAMESPACE_ID_CACHE_SIZE, vf.createLiteral(namespaceIDCacheSize));
		}

		return implNode;
	}

	@Override
	public void parse(Graph graph, Resource implNode)
		throws SailConfigException
	{
		super.parse(graph, implNode);

		try {
			Literal tripleIndexLit = GraphUtil.getOptionalObjectLiteral(graph, implNode, TRIPLE_INDEXES);
			if (tripleIndexLit != null) {
				setTripleIndexes((tripleIndexLit).getLabel());
			}

			Literal forceSyncLit = GraphUtil.getOptionalObjectLiteral(graph, implNode, FORCE_SYNC);
			if (forceSyncLit != null) {
				try {
					setForceSync(forceSyncLit.booleanValue());
				}
				catch (IllegalArgumentException e) {
					throw new SailConfigException("Boolean value required for " + FORCE_SYNC + " property, found "
							+ forceSyncLit);
				}
			}

			Literal valueCacheSizeLit = GraphUtil.getOptionalObjectLiteral(graph, implNode, VALUE_CACHE_SIZE);
			if (valueCacheSizeLit != null) {
				try {
					setValueCacheSize(valueCacheSizeLit.intValue());
				}
				catch (NumberFormatException e) {
					throw new SailConfigException("Integer value required for " + VALUE_CACHE_SIZE
							+ " property, found " + valueCacheSizeLit);
				}
			}

			Literal valueIDCacheSizeLit = GraphUtil.getOptionalObjectLiteral(graph, implNode,
					VALUE_ID_CACHE_SIZE);
			if (valueIDCacheSizeLit != null) {
				try {
					setValueIDCacheSize(valueIDCacheSizeLit.intValue());
				}
				catch (NumberFormatException e) {
					throw new SailConfigException("Integer value required for " + VALUE_ID_CACHE_SIZE
							+ " property, found " + valueIDCacheSizeLit);
				}
			}

			Literal namespaceCacheSizeLit = GraphUtil.getOptionalObjectLiteral(graph, implNode,
					NAMESPACE_CACHE_SIZE);
			if (namespaceCacheSizeLit != null) {
				try {
					setNamespaceCacheSize(namespaceCacheSizeLit.intValue());
				}
				catch (NumberFormatException e) {
					throw new SailConfigException("Integer value required for " + NAMESPACE_CACHE_SIZE
							+ " property, found " + namespaceCacheSizeLit);
				}
			}

			Literal namespaceIDCacheSizeLit = GraphUtil.getOptionalObjectLiteral(graph, implNode,
					NAMESPACE_ID_CACHE_SIZE);
			if (namespaceIDCacheSizeLit != null) {
				try {
					setNamespaceIDCacheSize(namespaceIDCacheSizeLit.intValue());
				}
				catch (NumberFormatException e) {
					throw new SailConfigException("Integer value required for " + NAMESPACE_ID_CACHE_SIZE
							+ " property, found " + namespaceIDCacheSizeLit);
				}
			}
		}
		catch (GraphUtilException e) {
			throw new SailConfigException(e.getMessage(), e);
		}
	}
}
