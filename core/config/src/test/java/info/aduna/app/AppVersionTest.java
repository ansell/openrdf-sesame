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
package info.aduna.app;

import static org.junit.Assert.*;

import org.junit.Test;


/**
 *
 * @author jeen
 */
public class AppVersionTest {

	@Test
	public void testCreateFromString() throws Exception {
		AppVersion v = new AppVersion("1.0.3");
		
		assertEquals(1, v.getMajor());
		assertEquals(0, v.getMinor());
		assertEquals(3, v.getMicro());
		assertNull(v.getModifier());
		
		v = new AppVersion("2.8.0-beta3-SNAPSHOT");
		assertEquals(2, v.getMajor());
		assertEquals(8, v.getMinor());
		assertEquals(0, v.getMicro());
		assertEquals("beta3-SNAPSHOT", v.getModifier());
	}

}
