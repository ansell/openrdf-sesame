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
package org.openrdf.repository.sail.memory;

import java.io.IOException;

import info.aduna.io.ResourceUtil;

import org.openrdf.IsolationLevel;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnectionTest;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.SailException;
import org.openrdf.sail.inferencer.fc.CustomGraphQueryInferencer;
import org.openrdf.sail.memory.MemoryStore;

public class CustomGraphQueryInferencerMemoryRepositoryConnectionTest extends RepositoryConnectionTest {

	public CustomGraphQueryInferencerMemoryRepositoryConnectionTest(IsolationLevel level) {
		super(level);
	}

	@Override
	protected Repository createRepository()
		throws MalformedQueryException, UnsupportedQueryLanguageException, SailException, IOException
	{
		return new SailRepository(new CustomGraphQueryInferencer(new MemoryStore(),
				QueryLanguage.SPARQL,
				ResourceUtil.getString("/testcases/custom-query-inferencing/predicate/rule.rq"),
				ResourceUtil.getString("/testcases/custom-query-inferencing/predicate/match.rq")));
	}
}