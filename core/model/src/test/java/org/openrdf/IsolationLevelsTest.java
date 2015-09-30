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
package org.openrdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * @author jeen
 */
public class IsolationLevelsTest {

	/**
	 * Test method for
	 * {@link org.openrdf.IsolationLevels#isCompatibleWith(org.openrdf.IsolationLevel)}
	 * .
	 */
	@Test
	public void testIsCompatibleWith() {
		assertTrue(IsolationLevels.SNAPSHOT.isCompatibleWith(IsolationLevels.READ_COMMITTED));
		assertTrue(IsolationLevels.SERIALIZABLE.isCompatibleWith(IsolationLevels.READ_COMMITTED));
		assertTrue(IsolationLevels.SNAPSHOT.isCompatibleWith(IsolationLevels.READ_UNCOMMITTED));
		assertFalse(IsolationLevels.READ_COMMITTED.isCompatibleWith(IsolationLevels.SNAPSHOT));
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
		supportedLevels.add(IsolationLevels.READ_UNCOMMITTED);
		supportedLevels.add(IsolationLevels.READ_COMMITTED);

		IsolationLevel compatibleLevel = IsolationLevels.getCompatibleIsolationLevel(
				IsolationLevels.SERIALIZABLE, supportedLevels);
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
