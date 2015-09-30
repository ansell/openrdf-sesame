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
package info.aduna.webapp.navigation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class NavigationTest {

	private NavigationModel model = null;

	@Before
	public void setUp() {
		model = new NavigationModel();
		List<String> navigationModelLocations = new ArrayList<String>();
		navigationModelLocations.add("/navigation.xml");
		model.setNavigationModels(navigationModelLocations);
	}

	@Test
	public void testParse() {
		assertNotNull("Parsed model is null", model);
		assertEquals("Model should have one group", 1, model.getGroups().size());
		Group systemGroup = model.getGroups().get(0);
		assertEquals("system group should have 1 subgroup", 1, systemGroup.getGroups().size());
		assertEquals("system group should have 2 views", 2, systemGroup.getViews().size());
		View loggingView = systemGroup.getViews().get(1);
		assertFalse("logging view should not be hidden", loggingView.isHidden());	
		assertTrue("logging view should be enabled", loggingView.isEnabled());	
		assertEquals("Path for logging is not correct", "/system/logging.view", loggingView.getPath());
		assertEquals("Icon for logging is not correct", "/images/icons/system_logging.png", loggingView.getIcon());
		assertEquals("I18N for logging is not correct", "system.logging.title", loggingView.getI18n());
		Group loggingGroup = systemGroup.getGroups().get(0);
		assertEquals("logging subgroup should have 1 views", 1, loggingGroup.getViews().size());	
		assertTrue("logging subgroup should be hidden", loggingGroup.isHidden());	
		assertTrue("logging subgroup should be enabled", loggingGroup.isEnabled());	
		View loggingOverview = loggingGroup.getViews().get(0);
		assertFalse("logging overview should be disabled", loggingOverview.isEnabled());	
	}

	@Test
	public void testFind() {
		assertNotNull("Find should have succeeded", model.findView("/system/logging/overview.view"));
		assertNull("Find should not have succeeded", model.findView("/system/logging/bogus.view"));
	}
}
