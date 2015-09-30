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
package org.openrdf.sail.helpers;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
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

	MemoryStore memoryStore = new MemoryStore();

	@Before
	public void before()
		throws SailException
	{
		memoryStore.initialize();
		wrapper = new NotifyingSailConnectionWrapper(memoryStore.getConnection());
		factory = memoryStore.getValueFactory();
	}

	@After
	public void after()
		throws SailException
	{
		wrapper.close();
		memoryStore.shutDown();
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
		wrapper.removeStatements(null, factory.createIRI("urn:pred"), factory.createLiteral(objectValue));
		wrapper.commit();
	}

	private void addStatement(String objectValue)
		throws SailException
	{
		wrapper.begin();
		wrapper.addStatement(factory.createBNode(), factory.createIRI("urn:pred"),
				factory.createLiteral(objectValue));
		wrapper.commit();
	}
}