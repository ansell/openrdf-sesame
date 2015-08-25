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
package org.openrdf.sail.config;

import static org.openrdf.sail.config.SailConfigSchema.SAILTYPE;

import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.model.util.ModelException;
import org.openrdf.model.util.Models;

/**
 * Base implementation of {@link SailImplConfig}
 * 
 * @author Herko ter Horst
 */
public abstract class AbstractSailImplConfig implements SailImplConfig {

	private String type;

	private long iterationCacheSyncThreshold;

	/**
	 * Create a new RepositoryConfigImpl.
	 */
	public AbstractSailImplConfig() {
	}

	/**
	 * Create a new RepositoryConfigImpl.
	 */
	public AbstractSailImplConfig(String type) {
		this();
		setType(type);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void validate()
		throws SailConfigException
	{
		if (type == null) {
			throw new SailConfigException("No type specified for sail implementation");
		}
	}

	public Resource export(Model m) {
		ValueFactory vf = SimpleValueFactory.getInstance();
		BNode implNode = vf.createBNode();

		if (type != null) {
			m.add(implNode, SAILTYPE, vf.createLiteral(type));
		}

		if (iterationCacheSyncThreshold > 0) {
			m.add(implNode, SailConfigSchema.ITERATION_CACHE_SYNC_THRESHOLD,
					vf.createLiteral(iterationCacheSyncThreshold));
		}

		return implNode;
	}

	public void parse(Model m, Resource implNode)
		throws SailConfigException
	{
		try {
			Models.objectLiteral(m.filter(implNode, SAILTYPE, null)).ifPresent(lit -> setType(lit.getLabel()));
			Models.objectLiteral(
					m.filter(implNode, SailConfigSchema.ITERATION_CACHE_SYNC_THRESHOLD, null)).ifPresent(
							lit -> setIterationCacheSyncThreshold(lit.longValue()));
		}
		catch (ModelException e) {
			throw new SailConfigException(e.getMessage(), e);
		}
	}

	/**
	 * @return Returns the iterationCacheSize.
	 */
	public long getIterationCacheSyncThreshold() {
		return iterationCacheSyncThreshold;
	}

	/**
	 * @param iterationCacheSyncThreshold
	 *        The iterationCacheSyncThreshold to set.
	 */
	public void setIterationCacheSyncThreshold(long iterationCacheSyncThreshold) {
		this.iterationCacheSyncThreshold = iterationCacheSyncThreshold;
	}
}
