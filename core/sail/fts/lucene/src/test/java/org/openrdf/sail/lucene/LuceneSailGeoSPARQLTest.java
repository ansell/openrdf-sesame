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
package org.openrdf.sail.lucene;

import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

public class LuceneSailGeoSPARQLTest extends AbstractLuceneSailGeoSPARQLTest {

	private LuceneIndex index;

	@Override
	protected void configure(LuceneSail sail)
		throws IOException
	{
		index = new LuceneIndex(new RAMDirectory(), new StandardAnalyzer());
		sail.setLuceneIndex(index);
	}


	@Test
	@Override
	public void testIntersectionQuery()
		throws RepositoryException, MalformedQueryException, QueryEvaluationException
	{
		super.testIntersectionQuery();
	}


	@Test
	@Override
	public void testComplexIntersectionQuery()
		throws RepositoryException, MalformedQueryException, QueryEvaluationException
	{
		super.testComplexIntersectionQuery();
	}
}
