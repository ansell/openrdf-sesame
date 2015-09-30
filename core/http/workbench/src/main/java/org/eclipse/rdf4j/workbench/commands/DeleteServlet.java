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
package org.eclipse.rdf4j.workbench.commands;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.rdf4j.query.QueryResultHandlerException;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.manager.RepositoryInfo;
import org.eclipse.rdf4j.workbench.base.TransformationServlet;
import org.eclipse.rdf4j.workbench.util.TupleResultBuilder;
import org.eclipse.rdf4j.workbench.util.WorkbenchRequest;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Servlet responsible for presenting the list of repositories, and deleting the
 * chosen one.
 */
public class DeleteServlet extends TransformationServlet {

	/**
	 * Deletes the repository with the given ID, then redirects to the repository
	 * selection page. If given a "checkSafe" parameter, instead returns JSON
	 * response with safe field set to true if safe, false if not.
	 */
	@Override
	protected void doPost(WorkbenchRequest req, HttpServletResponse resp, String xslPath)
		throws Exception
	{
		dropRepository(req.getParameter("id"));
		resp.sendRedirect("../");
	}

	@Override
	protected void service(WorkbenchRequest req, HttpServletResponse resp, String xslPath)
		throws Exception
	{
		String checkSafe = req.getParameter("checkSafe");
		if (null == checkSafe) {
			// Display the form.
			super.service(req, resp, xslPath);
		}
		else {
			// Respond to 'checkSafe' XmlHttpRequest with JSON.
			final PrintWriter writer = new PrintWriter(new BufferedWriter(resp.getWriter()));
			writer.write(new JSONObject().put("safe", manager.isSafeToRemove(checkSafe)).toString());
			writer.flush();
		}

	}

	private void dropRepository(String identity)
		throws RepositoryException, RepositoryConfigException
	{
		manager.removeRepository(identity);
	}

	/**
	 * Presents a page where the user can choose a repository ID to delete.
	 */
	@Override
	public void service(TupleResultBuilder builder, String xslPath)
		throws RepositoryException, QueryResultHandlerException
	{
		builder.transform(xslPath, "delete.xsl");
		builder.start("readable", "writeable", "id", "description", "location");
		builder.link(Arrays.asList(INFO));
		for (RepositoryInfo info : manager.getAllRepositoryInfos()) {
			builder.result(info.isReadable(), info.isWritable(), info.getId(), info.getDescription(),
					info.getLocation());
		}
		builder.end();
	}

}
