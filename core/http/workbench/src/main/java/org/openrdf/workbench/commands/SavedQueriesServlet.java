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

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.OpenRDFException;
import org.openrdf.model.IRI;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.exceptions.BadRequestException;
import org.openrdf.workbench.util.QueryStorage;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

/**
 * Servlet that provides a page to access saved queries.
 * 
 * @author Dale Visser
 */
public class SavedQueriesServlet extends TransformationServlet {

	private QueryStorage storage;

	@Override
	public String[] getCookieNames() {
		return new String[] { QueryServlet.LIMIT, "queryLn", "infer", "total_result_count" };
	}

	@Override
	public void init(final ServletConfig config)
		throws ServletException
	{
		super.init(config);
		try {
			this.storage = QueryStorage.getSingletonInstance(this.appConfig);
		}
		catch (RepositoryException e) {
			throw new ServletException(e);
		}
		catch (IOException e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void service(final WorkbenchRequest req, final HttpServletResponse resp, final String xslPath)
		throws IOException, OpenRDFException, BadRequestException
	{
		final TupleResultBuilder builder = getTupleResultBuilder(req, resp, resp.getOutputStream());
		builder.transform(xslPath, "saved-queries.xsl");
		builder.start();
		builder.link(Arrays.asList(INFO));
		this.getSavedQueries(req, builder);
		builder.end();
	}

	@Override
	protected void doPost(final WorkbenchRequest wreq, final HttpServletResponse resp, final String xslPath)
		throws BadRequestException, IOException, OpenRDFException
	{
		final String urn = wreq.getParameter("delete");
		if (null == urn || urn.isEmpty()) {
			throw new BadRequestException("Expected POST to contain a 'delete=' parameter.");
		}
		final boolean accessible = storage.checkAccess((HTTPRepository)this.repository);
		if (accessible) {
			String userName = wreq.getParameter(SERVER_USER);
			if (null == userName) {
				userName = "";
			}
			final IRI queryURI = SimpleValueFactory.getInstance().createIRI(urn);
			if (storage.canChange(queryURI, userName)) {
				storage.deleteQuery(queryURI, userName);
			}
			else {
				throw new BadRequestException("User '" + userName + "' may not delete query id " + urn);
			}
		}
		this.service(wreq, resp, xslPath);
	}

	private void getSavedQueries(final WorkbenchRequest req, final TupleResultBuilder builder)
		throws OpenRDFException, BadRequestException
	{
		final HTTPRepository repo = (HTTPRepository)this.repository;
		String user = req.getParameter(SERVER_USER);
		if (null == user) {
			user = "";
		}
		if (!storage.checkAccess(repo)) {
			throw new BadRequestException(
					"User '" + user + "' not authorized to access repository '" + repo.getRepositoryURL() + "'");
		}
		storage.selectSavedQueries(repo, user, builder);
	}
}