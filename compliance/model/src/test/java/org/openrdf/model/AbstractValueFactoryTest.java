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
package org.openrdf.model;

import org.junit.After;
import org.junit.Before;

/**
 * Abstract tests for the {@link ValueFactory} interface, including concurrency
 * tests, when these are enabled.
 * 
 * @author Peter Ansell
 */
public abstract class AbstractValueFactoryTest {

	private ValueFactory vf;

	@Before
	public void setUp()
		throws Exception
	{
		vf = getNewValueFactory();
	}

	@After
	public void tearDown()
		throws Exception
	{
		vf = null;
	}

	/**
	 * Implementing tests must override this method to provide an implementation
	 * of the {@link ValueFactory} interface.
	 * 
	 * @return A new instance of the {@link ValueFactory} interface.
	 */
	protected abstract ValueFactory getNewValueFactory();

	/**
	 * Determines whether to enable the concurrency tests for this
	 * implementation.
	 * 
	 * @return True if the implementation is thought to be threadsafe, and should
	 *         be tested using concurrent access from different threads.
	 */
	protected abstract boolean isThreadSafe();

}
