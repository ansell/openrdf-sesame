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
package org.openrdf.sail.memory;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.CustomGraphQueryInferencerTest;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.SailException;

public class MemCustomInferencingTest extends TestCase {

	public static Test suite()
		throws MalformedQueryException, UnsupportedQueryLanguageException, SailException, RepositoryException,
		IOException
	{
		NotifyingSail store = new MemoryStore();
		TestSuite suite = new TestSuite(MemCustomInferencingTest.class.getName());
		CustomGraphQueryInferencerTest.addTests(suite, store);
		return suite;
	}
}