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
package org.openrdf.sail.nativerdf.config;

import static org.openrdf.sail.nativerdf.config.NativeStoreSchema.FORCE_SYNC;
import static org.openrdf.sail.nativerdf.config.NativeStoreSchema.NAMESPACE_CACHE_SIZE;
import static org.openrdf.sail.nativerdf.config.NativeStoreSchema.NAMESPACE_ID_CACHE_SIZE;
import static org.openrdf.sail.nativerdf.config.NativeStoreSchema.TRIPLE_INDEXES;
import static org.openrdf.sail.nativerdf.config.NativeStoreSchema.VALUE_CACHE_SIZE;
import static org.openrdf.sail.nativerdf.config.NativeStoreSchema.VALUE_ID_CACHE_SIZE;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.util.ModelException;
import org.openrdf.model.util.Models;
import org.openrdf.sail.config.AbstractSailImplConfig;
import org.openrdf.sail.config.SailConfigException;

/**
 * @author Arjohn Kampman
 */
public class NativeStoreConfig extends AbstractSailImplConfig {

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
	public Resource export(Model m) {
		Resource implNode = super.export(m);
		ValueFactory vf = SimpleValueFactory.getInstance();

		if (tripleIndexes != null) {
			m.add(implNode, TRIPLE_INDEXES, vf.createLiteral(tripleIndexes));
		}
		if (forceSync) {
			m.add(implNode, FORCE_SYNC, vf.createLiteral(forceSync));
		}
		if (valueCacheSize >= 0) {
			m.add(implNode, VALUE_CACHE_SIZE, vf.createLiteral(valueCacheSize));
		}
		if (valueIDCacheSize >= 0) {
			m.add(implNode, VALUE_ID_CACHE_SIZE, vf.createLiteral(valueIDCacheSize));
		}
		if (namespaceCacheSize >= 0) {
			m.add(implNode, NAMESPACE_CACHE_SIZE, vf.createLiteral(namespaceCacheSize));
		}
		if (namespaceIDCacheSize >= 0) {
			m.add(implNode, NAMESPACE_ID_CACHE_SIZE, vf.createLiteral(namespaceIDCacheSize));
		}

		return implNode;
	}

	@Override
	public void parse(Model m, Resource implNode)
		throws SailConfigException
	{
		super.parse(m, implNode);

		try {
			
			Models.objectLiteral(m.filter(implNode, TRIPLE_INDEXES, null)).ifPresent(lit -> setTripleIndexes(lit.getLabel()));
			Models.objectLiteral(m.filter(implNode, FORCE_SYNC, null)).ifPresent(lit -> {
				try {
					setForceSync(lit.booleanValue());
				}
				catch (IllegalArgumentException e) {
					throw new SailConfigException("Boolean value required for " + FORCE_SYNC + " property, found "
							+ lit);
				}
			});

			Models.objectLiteral(m.filter(implNode, VALUE_CACHE_SIZE, null)).ifPresent(lit -> {
				try {
					setValueCacheSize(lit.intValue());
				}
				catch (NumberFormatException e) {
					throw new SailConfigException("Integer value required for " + VALUE_CACHE_SIZE
							+ " property, found " + lit);
				}
			});
			
			Models.objectLiteral(m.filter(implNode, VALUE_ID_CACHE_SIZE, null)).ifPresent(lit -> {
				try {
					setValueIDCacheSize(lit.intValue());
				}
				catch (NumberFormatException e) {
					throw new SailConfigException("Integer value required for " + VALUE_ID_CACHE_SIZE
							+ " property, found " + lit);
				}
			});
			

			Models.objectLiteral(m.filter(implNode, NAMESPACE_CACHE_SIZE, null)).ifPresent(lit -> {
				try {
					setNamespaceCacheSize(lit.intValue());
				}
				catch (NumberFormatException e) {
					throw new SailConfigException("Integer value required for " + NAMESPACE_CACHE_SIZE
							+ " property, found " + lit);
				}
			});

			Models.objectLiteral(m.filter(implNode, NAMESPACE_ID_CACHE_SIZE, null)).ifPresent(lit -> {
				try {
					setNamespaceIDCacheSize(lit.intValue());
				}
				catch (NumberFormatException e) {
					throw new SailConfigException("Integer value required for " + NAMESPACE_ID_CACHE_SIZE
							+ " property, found " + lit);
				}
			});
		}
		catch (ModelException e) {
			throw new SailConfigException(e.getMessage(), e);
		}
	}
}
