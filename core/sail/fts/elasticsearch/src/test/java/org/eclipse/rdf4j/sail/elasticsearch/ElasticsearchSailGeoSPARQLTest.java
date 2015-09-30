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
package org.eclipse.rdf4j.sail.elasticsearch;

import java.io.File;
import java.io.IOException;

import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.sail.elasticsearch.ElasticsearchIndex;
import org.eclipse.rdf4j.sail.lucene.AbstractLuceneSailGeoSPARQLTest;
import org.eclipse.rdf4j.sail.lucene.LuceneSail;
import org.elasticsearch.common.io.FileSystemUtils;
import org.junit.Ignore;
import org.junit.Test;

public class ElasticsearchSailGeoSPARQLTest extends AbstractLuceneSailGeoSPARQLTest {

	private static final String DATA_DIR = "target/test-data";

	@Override
	protected void configure(LuceneSail sail) {
		sail.setParameter(LuceneSail.INDEX_CLASS_KEY, ElasticsearchIndex.class.getName());
		sail.setParameter(LuceneSail.LUCENE_DIR_KEY, DATA_DIR);
		sail.setParameter(ElasticsearchIndex.WAIT_FOR_STATUS_KEY, "green");
		sail.setParameter(ElasticsearchIndex.WAIT_FOR_NODES_KEY, ">=1");
	}

	@Test
	@Ignore // JTS is required
	@Override
	public void testIntersectionQuery()
		throws RepositoryException, MalformedQueryException, QueryEvaluationException
	{
		super.testIntersectionQuery();
	}


	@Test
	@Ignore // JTS is required
	@Override
	public void testComplexIntersectionQuery()
		throws RepositoryException, MalformedQueryException, QueryEvaluationException
	{
		super.testComplexIntersectionQuery();
	}

	@Override
	public void tearDown()
		throws IOException, RepositoryException
	{
		super.tearDown();
		FileSystemUtils.deleteRecursively(new File(DATA_DIR));
	}
}
