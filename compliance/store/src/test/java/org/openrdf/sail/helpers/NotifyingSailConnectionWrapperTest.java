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
package org.openrdf.sail.helpers;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.SailException;
import org.openrdf.sail.memory.MemoryStore;

/**
 * Some general tests for {@link NogifyingSailConnectionWrapper} expected
 * behaviour.
 * 
 * @author Dale Visser
 */
public class NotifyingSailConnectionWrapperTest {

	/**
	 * @author Dale Visser
	 */
	private final class TestListener implements SailConnectionListener {

		int testCounter = 0;

		@Override
		public void statementAdded(Statement st) {
			testCounter++;
		}

		@Override
		public void statementRemoved(Statement st) {
			testCounter--;
		}

		public int getCount() {
			return testCounter;
		}
	}

	NotifyingSailConnectionWrapper wrapper;

	ValueFactory factory;

	TestListener listener = new TestListener();

	@Before
	public void before()
		throws SailException
	{
		MemoryStore memoryStore = new MemoryStore();
		memoryStore.initialize();
		NotifyingSailConnection connection = memoryStore.getConnection();
		wrapper = new NotifyingSailConnectionWrapper(connection);
		factory = memoryStore.getValueFactory();
	}

	@After
	public void after()
		throws SailException
	{
		wrapper.close();
	}

	/**
	 * Regression test for SES-1934.
	 * 
	 * @throws SailException
	 */
	@Test
	public void testAddThenRemoveListener()
		throws SailException
	{
		wrapper.addConnectionListener(listener);
		addStatement("a");
		assertThat(listener.getCount(), is(equalTo(1)));
		removeStatement("a");
		assertThat(listener.getCount(), is(equalTo(0)));
		wrapper.removeConnectionListener(listener);
		addStatement("b");
		assertThat(listener.getCount(), is(equalTo(0)));
	}

	private void removeStatement(String objectValue)
		throws SailException
	{
		wrapper.begin();
		wrapper.removeStatements(null, factory.createURI("urn:pred"), factory.createLiteral(objectValue));
		wrapper.commit();
	}

	private void addStatement(String objectValue)
		throws SailException
	{
		wrapper.begin();
		wrapper.addStatement(factory.createBNode(), factory.createURI("urn:pred"),
				factory.createLiteral(objectValue));
		wrapper.commit();
	}
}