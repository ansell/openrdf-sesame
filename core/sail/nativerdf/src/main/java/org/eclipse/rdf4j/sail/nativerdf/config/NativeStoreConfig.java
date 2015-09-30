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
package org.eclipse.rdf4j.sail.nativerdf.config;

import static org.eclipse.rdf4j.sail.nativerdf.config.NativeStoreSchema.FORCE_SYNC;
import static org.eclipse.rdf4j.sail.nativerdf.config.NativeStoreSchema.NAMESPACE_CACHE_SIZE;
import static org.eclipse.rdf4j.sail.nativerdf.config.NativeStoreSchema.NAMESPACE_ID_CACHE_SIZE;
import static org.eclipse.rdf4j.sail.nativerdf.config.NativeStoreSchema.TRIPLE_INDEXES;
import static org.eclipse.rdf4j.sail.nativerdf.config.NativeStoreSchema.VALUE_CACHE_SIZE;
import static org.eclipse.rdf4j.sail.nativerdf.config.NativeStoreSchema.VALUE_ID_CACHE_SIZE;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelException;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.sail.config.AbstractSailImplConfig;
import org.eclipse.rdf4j.sail.config.SailConfigException;

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
