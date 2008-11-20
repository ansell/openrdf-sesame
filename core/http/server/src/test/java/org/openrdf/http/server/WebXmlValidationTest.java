/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;

import junit.framework.TestCase;

import info.aduna.xml.DocumentUtil;

/**
 * @author Herko ter Horst
 */
public class WebXmlValidationTest extends TestCase {

	public void testValidXml() {
		try {
			File webXml = new File("src/main/webapp/WEB-INF/web.xml");

			DocumentUtil.getDocument(webXml.toURI().toURL(), SchemaFactory.newInstance(
					XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema());
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
