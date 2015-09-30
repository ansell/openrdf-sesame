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
