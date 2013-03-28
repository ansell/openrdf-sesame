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

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.vocabulary.SESAME;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.workbench.util.WorkbenchRequest;

/**
 * @author dale
 */
public class TestInfoServlet {

	private final InfoServlet servlet = new InfoServlet();

	private RepositoryManager manager;

	private final RepositoryInfo info = new RepositoryInfo();

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		servlet.setRepositoryInfo(info);
		manager = mock(RepositoryManager.class);
		servlet.setRepositoryManager(manager);
	}

	/**
	 * Throwing exceptions for invalid repository ID's results in a 500 response
	 * code to the client. As seen in the bug report, some versions of Internet
	 * Explorer don't gracefully handle error responses during XSLT parsing.
	 * 
	 * @see <a href="https://openrdf.atlassian.net/browse/SES-1770">SES-1770</a>
	 */
	@Test
	public final void testSES1770regression()
		throws Exception
	{
		when(manager.hasRepositoryConfig(null)).thenThrow(new NullPointerException());
		WorkbenchRequest req = mock(WorkbenchRequest.class);
		when(req.getParameter(anyString())).thenReturn(SESAME.NIL.toString());
		HttpServletResponse resp = mock(HttpServletResponse.class);
		when(resp.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
		servlet.service(req, resp, "");
	}

}
