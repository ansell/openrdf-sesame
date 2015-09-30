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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.rdf4j.query.QueryResultHandlerException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.workbench.base.TransformationServlet;
import org.eclipse.rdf4j.workbench.exceptions.BadRequestException;
import org.eclipse.rdf4j.workbench.util.TupleResultBuilder;
import org.eclipse.rdf4j.workbench.util.WorkbenchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClearServlet extends TransformationServlet {

	private final Logger logger = LoggerFactory.getLogger(ClearServlet.class);

	@Override
	protected void doPost(WorkbenchRequest req, HttpServletResponse resp, String xslPath)
		throws IOException, RepositoryException, QueryResultHandlerException
	{
		try {
			RepositoryConnection con = repository.getConnection();
			try {
				if (req.isParameterPresent(CONTEXT)) {
					con.clear(req.getResource(CONTEXT));
				}
				else {
					con.clear();
				}
			}
			catch (ClassCastException exc) {
				throw new BadRequestException(exc.getMessage(), exc);
			}
			finally {
				con.close();
			}
			resp.sendRedirect("summary");
		}
		catch (BadRequestException exc) {
			logger.warn(exc.toString(), exc);
			TupleResultBuilder builder = getTupleResultBuilder(req, resp, resp.getOutputStream());
			builder.transform(xslPath, "clear.xsl");
			builder.start("error-message", CONTEXT);
			builder.link(Arrays.asList(INFO));
			builder.result(exc.getMessage(), req.getParameter(CONTEXT));
			builder.end();
		}
	}

	@Override
	public void service(TupleResultBuilder builder, String xslPath)
		throws RepositoryException, QueryResultHandlerException
	{
		// TupleResultBuilder builder = new TupleResultBuilder(out);
		builder.transform(xslPath, "clear.xsl");
		builder.start();
		builder.link(Arrays.asList(INFO));
		builder.end();
	}

}