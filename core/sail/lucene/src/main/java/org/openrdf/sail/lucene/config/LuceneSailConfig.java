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
import org.openrdf.model.Resource;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.sail.config.DelegatingSailImplConfigBase;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailImplConfig;

public class LuceneSailConfig extends DelegatingSailImplConfigBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private String indexDir;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public LuceneSailConfig() {
		super(LuceneSailFactory.SAIL_TYPE);
	}

	public LuceneSailConfig(SailImplConfig delegate) {
		super(LuceneSailFactory.SAIL_TYPE, delegate);
	}

	public LuceneSailConfig(String luceneDir) {
		super(LuceneSailFactory.SAIL_TYPE);
		setIndexDir(luceneDir);
	}

	public LuceneSailConfig(String luceneDir, SailImplConfig delegate) {
		super(LuceneSailFactory.SAIL_TYPE, delegate);
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
	public Resource export(Graph graph) {
		Resource implNode = super.export(graph);

		if (indexDir != null) {
			graph.add(implNode, INDEX_DIR, graph.getValueFactory().createLiteral(indexDir));
		}

		return implNode;
	}

	@Override
	public void parse(Graph graph, Resource implNode)
		throws SailConfigException
	{
		super.parse(graph, implNode);

		try {
			Literal indexDirLit = GraphUtil.getOptionalObjectLiteral(graph, implNode, INDEX_DIR);
			if (indexDirLit != null) {
				setIndexDir(indexDirLit.getLabel());
			}
		}
		catch (GraphUtilException e) {
			throw new SailConfigException(e.getMessage(), e);
		}
	}
}
