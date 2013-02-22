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
package org.openrdf.runtime;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.openrdf.OpenRDFException;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RepositoryManager;

/**
 * @author Dale Visser
 */
public class TestRepositoryManagerFederator {

	RepositoryManagerFederator federator;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		RepositoryManager manager = mock(RepositoryManager.class);
		Repository system = mock(Repository.class);
		when(system.getValueFactory()).thenReturn(ValueFactoryImpl.getInstance());
		when(manager.getSystemRepository()).thenReturn(system);
		federator = new RepositoryManagerFederator(manager);
	}

	@Test
	public final void testDirectRecursiveAddThrowsException()
		throws MalformedURLException, OpenRDFException
	{
		thrown.expect(is(instanceOf(RepositoryConfigException.class)));
		thrown.expectMessage(is(equalTo("A federation member may not have the same ID as the federation.")));
		String id = "fedtest";
		federator.addFed(id, "Federation Test", Arrays.asList(new String[] { id, "ignore" }), true, false);
	}

}
