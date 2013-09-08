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
package org.openrdf.workbench.commands;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import org.openrdf.repository.config.RepositoryConfig;

/**
 * @author Dale Visser
 */
public class TestCreateServlet {

	/**
	 * Regression test for SES-1907.
	 */
	@Test
	public final void testExpectedTemplatesCanBeResolved() {
		String[] expectedTemplates = {
				"memory-customrule",
				"memory-rdfs-dt",
				"memory-rdfs",
				"memory",
				"mysql",
				"native-customrule",
				"native-rdfs-dt",
				"native-rdfs",
				"native",
				"pgsql",
				"remote",
				"sparql" };
		for (String template : expectedTemplates) {
			String resource = template + ".ttl";
			assertThat(resource, RepositoryConfig.class.getResourceAsStream(resource), is(notNullValue()));
		}
	}
}
