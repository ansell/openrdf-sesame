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
package org.openrdf.sail.solr.config;

import org.openrdf.sail.Sail;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailFactory;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.lucene.LuceneSail;
import org.openrdf.sail.lucene.config.AbstractLuceneSailConfig;
import org.openrdf.sail.solr.SolrIndex;

/**
 * A {@link SailFactory} that creates {@link LuceneSail}s based on RDF
 * configuration data.
 */
public class SolrSailFactory implements SailFactory {

	/**
	 * The type of repositories that are created by this factory.
	 * 
	 * @see SailFactory#getSailType()
	 */
	public static final String SAIL_TYPE = "openrdf:LuceneSail/Solr";

	/**
	 * Returns the Sail's type: <tt>openrdf:LuceneSail/Solr</tt>.
	 */
	@Override
	public String getSailType() {
		return SAIL_TYPE;
	}

	@Override
	public SailImplConfig getConfig() {
		return new SolrSailConfig();
	}

	@Override
	public Sail getSail(SailImplConfig config)
		throws SailConfigException
	{
		if (!SAIL_TYPE.equals(config.getType())) {
			throw new SailConfigException("Invalid Sail type: " + config.getType());
		}

		LuceneSail luceneSail = new LuceneSail();
		luceneSail.setParameter(LuceneSail.INDEX_CLASS_KEY, SolrIndex.class.getName());

		if (config instanceof AbstractLuceneSailConfig) {
			AbstractLuceneSailConfig luceneConfig = (AbstractLuceneSailConfig)config;
			luceneSail.setParameter(LuceneSail.LUCENE_DIR_KEY, luceneConfig.getIndexDir());
		}

		return luceneSail;
	}
}
