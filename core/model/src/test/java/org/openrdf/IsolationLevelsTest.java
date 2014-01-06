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
package org.openrdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jeen
 */
public class IsolationLevelsTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown()
		throws Exception
	{
	}

	/**
	 * Test method for
	 * {@link org.openrdf.IsolationLevels#isCompatibleWith(org.openrdf.IsolationLevel)}
	 * .
	 */
	@Test
	public void testIsCompatibleWith() {
		assertTrue(IsolationLevels.SNAPSHOT.isCompatibleWith(IsolationLevels.READ_COMMITTED));
		assertTrue(IsolationLevels.SERIALIZABLE.isCompatibleWith(IsolationLevels.READ_COMMITTED));
		assertFalse(IsolationLevels.SNAPSHOT.isCompatibleWith(IsolationLevels.READ_UNCOMMITTED));
	}

	/**
	 * Test method for
	 * {@link org.openrdf.IsolationLevels#getCompatibleIsolationLevel(org.openrdf.IsolationLevel, java.util.List)}
	 * .
	 */
	@Test
	public void testGetCompatibleIsolationLevel() {

		List<IsolationLevels> supportedLevels = new ArrayList<IsolationLevels>();
		supportedLevels.add(IsolationLevels.NONE);
		supportedLevels.add(IsolationLevels.SERIALIZABLE);

		IsolationLevel compatibleLevel = IsolationLevels.getCompatibleIsolationLevel(
				IsolationLevels.READ_COMMITTED, supportedLevels);
		assertNotNull(compatibleLevel);
		assertEquals(IsolationLevels.SERIALIZABLE, compatibleLevel);
	}

	@Test
	public void testGetCompatibleIsolationLevelNoneFound() {

		List<IsolationLevels> supportedLevels = new ArrayList<IsolationLevels>();
		supportedLevels.add(IsolationLevels.NONE);
		supportedLevels.add(IsolationLevels.SNAPSHOT);
		supportedLevels.add(IsolationLevels.SERIALIZABLE);

		IsolationLevel compatibleLevel = IsolationLevels.getCompatibleIsolationLevel(
				IsolationLevels.READ_UNCOMMITTED, supportedLevels);
		assertNull(compatibleLevel);

	}

	@Test
	public void testGetCompatibleIsolationLevelNullParams() {
		try {
			IsolationLevel compatibleLevel = IsolationLevels.getCompatibleIsolationLevel(
					IsolationLevels.SNAPSHOT, null);
			fail("should have resulted in an IllegalArgumentException");
		}
		catch (IllegalArgumentException e) {
			// do nothing, expected.
		}

		List<IsolationLevels> supportedLevels = new ArrayList<IsolationLevels>();
		supportedLevels.add(IsolationLevels.NONE);
		supportedLevels.add(IsolationLevels.SNAPSHOT);
		supportedLevels.add(IsolationLevels.SERIALIZABLE);

		try {
			IsolationLevel compatibleLevel = IsolationLevels.getCompatibleIsolationLevel(null, supportedLevels);
			fail("should have resulted in an IllegalArgumentException");
		}
		catch (IllegalArgumentException e) {
			// do nothing, expected.
		}
	}

}
