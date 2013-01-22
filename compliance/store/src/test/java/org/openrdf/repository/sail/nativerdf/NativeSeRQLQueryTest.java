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
package org.openrdf.repository.sail.nativerdf;

import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.Test;

import info.aduna.io.FileUtil;

import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.serql.SeRQLQueryTestCase;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.nativerdf.NativeStore;

public class NativeSeRQLQueryTest extends SeRQLQueryTestCase {

	public static Test suite()
		throws Exception
	{
		return SeRQLQueryTestCase.suite(new Factory() {

			public Test createTest(String name, String dataFile, List<String> graphNames, String queryFile,
					String resultFile, String entailment)
			{
				return new NativeSeRQLQueryTest(name, dataFile, graphNames, queryFile, resultFile, entailment);
			}
		});
	}

	private File dataDir;

	public NativeSeRQLQueryTest(String name, String dataFile, List<String> graphNames, String queryFile,
			String resultFile, String entailment)
	{
		super(name, dataFile, graphNames, queryFile, resultFile, entailment);
	}

	@Override
	protected QueryLanguage getQueryLanguage() {
		return QueryLanguage.SERQL;
	}

	@Override
	protected NotifyingSail newSail()
		throws IOException
	{
		dataDir = FileUtil.createTempDir("nativestore");
		return new NativeStore(dataDir, "spoc");
	}
}
