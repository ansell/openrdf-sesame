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
package org.openrdf.sail.lucene.config;

import static org.openrdf.sail.lucene.config.LuceneSailConfigSchema.INDEX_DIR;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.model.util.Models;
import org.openrdf.sail.config.AbstractDelegatingSailImplConfig;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailImplConfig;

public abstract class AbstractLuceneSailConfig extends AbstractDelegatingSailImplConfig {
	/*-----------*
	 * Variables *
	 *-----------*/

	private String indexDir;

	/*--------------*
	 * Constructors *
	 *--------------*/

	protected AbstractLuceneSailConfig(String type) {
		super(type);
	}

	protected AbstractLuceneSailConfig(String type, SailImplConfig delegate) {
		super(type, delegate);
	}

	protected AbstractLuceneSailConfig(String type, String luceneDir) {
		super(type);
		setIndexDir(luceneDir);
	}

	protected AbstractLuceneSailConfig(String type, String luceneDir, SailImplConfig delegate) {
		super(type, delegate);
		setIndexDir(luceneDir);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public String getIndexDir() {
		return indexDir;
	}

	public void setIndexDir(String luceneDir) {
		this.indexDir = luceneDir;
	}

	@Override
	public Resource export(Model m) {
		Resource implNode = super.export(m);

		if (indexDir != null) {
			m.add(implNode, INDEX_DIR, SimpleValueFactory.getInstance().createLiteral(indexDir));
		}

		return implNode;
	}

	@Override
	public void parse(Model graph, Resource implNode)
		throws SailConfigException
	{
		super.parse(graph, implNode);

		Literal indexDirLit = Models.objectLiteral(graph.filter(implNode, INDEX_DIR, null)).orElseThrow(
				() -> new SailConfigException("no value found for " + INDEX_DIR));

		setIndexDir(indexDirLit.getLabel());
	}
}
