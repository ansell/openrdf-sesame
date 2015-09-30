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
package org.eclipse.rdf4j.sail.config;

import static org.eclipse.rdf4j.sail.config.SailConfigSchema.SAILTYPE;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Graph;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.GraphUtil;
import org.eclipse.rdf4j.model.util.GraphUtilException;
import org.eclipse.rdf4j.model.util.ModelException;
import org.eclipse.rdf4j.model.util.Models;

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
