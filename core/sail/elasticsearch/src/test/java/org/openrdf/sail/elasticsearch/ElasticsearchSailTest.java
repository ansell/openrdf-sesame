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
package org.openrdf.sail.elasticsearch;

import java.io.File;
import java.io.IOException;

import org.elasticsearch.common.io.FileSystemUtils;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.lucene.AbstractLuceneSailTest;
import org.openrdf.sail.lucene.LuceneSail;

public class ElasticsearchSailTest extends AbstractLuceneSailTest {
	private static final String DATA_DIR = "target/test-data";

	@Override
	protected void configure(LuceneSail sail)
	{
		sail.setParameter(LuceneSail.INDEX_CLASS_KEY, ElasticsearchIndex.class.getName());
		sail.setParameter(LuceneSail.LUCENE_DIR_KEY, DATA_DIR);
	}

	public void tearDown() throws IOException, RepositoryException
	{
		super.tearDown();
		FileSystemUtils.deleteRecursively(new File(DATA_DIR));
	}
}
