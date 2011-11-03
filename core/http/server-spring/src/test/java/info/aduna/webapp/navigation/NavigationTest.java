/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.webapp.navigation;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class NavigationTest extends TestCase {
	
	private NavigationModel model = null;
	
	public void setUp() {
		model = new NavigationModel();
		List<String> navigationModelLocations = new ArrayList<String>();
		navigationModelLocations.add("/navigation.xml");
		model.setNavigationModels(navigationModelLocations);
	}
	
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
	
	public void testFind() {
		assertNotNull("Find should have succeeded", model.findView("/system/logging/overview.view"));
		assertNull("Find should not have succeeded", model.findView("/system/logging/bogus.view"));
	}
}
