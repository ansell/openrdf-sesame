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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Resource;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.exceptions.BadRequestException;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public class AddServlet extends TransformationServlet {

	private static final String URL = "url";

	private final Logger logger = LoggerFactory.getLogger(AddServlet.class);

	@Override
	protected void doPost(WorkbenchRequest req, HttpServletResponse resp, String xslPath)
		throws IOException, RepositoryException, FileUploadException, QueryResultHandlerException
	{
		try {
			String baseURI = req.getParameter("baseURI");
			String contentType = req.getParameter("Content-Type");
			if (req.isParameterPresent(CONTEXT)) {
				Resource context = req.getResource(CONTEXT);
				if (req.isParameterPresent(URL)) {
					add(req.getUrl(URL), baseURI, contentType, context);
				}
				else {
					add(req.getContentParameter(), baseURI, contentType, req.getContentFileName(), context);
				}
			}
			else {
				if (req.isParameterPresent(URL)) {
					add(req.getUrl(URL), baseURI, contentType);
				}
				else {
					add(req.getContentParameter(), baseURI, contentType, req.getContentFileName());
				}
			}
			resp.sendRedirect("summary");
		}
		catch (BadRequestException exc) {
			logger.warn(exc.toString(), exc);
			TupleResultBuilder builder = getTupleResultBuilder(req, resp, resp.getOutputStream());
			builder.transform(xslPath, "add.xsl");
			builder.start("error-message", "baseURI", CONTEXT, "Content-Type");
			builder.link(Arrays.asList(INFO));
			String baseURI = req.getParameter("baseURI");
			String context = req.getParameter(CONTEXT);
			String contentType = req.getParameter("Content-Type");
			builder.result(exc.getMessage(), baseURI, context, contentType);
			builder.end();
		}
	}

	private void add(InputStream stream, String baseURI, String contentType, String contentFileName,
			Resource... context)
		throws BadRequestException, RepositoryException, IOException
	{
		if (contentType == null) {
			throw new BadRequestException("No Content-Type provided");
		}

		RDFFormat format = null;
		if ("autodetect".equals(contentType)) {
			format = Rio.getParserFormatForFileName(contentFileName).orElseThrow(
					() -> new BadRequestException("Could not automatically determine Content-Type for content: "
							+ contentFileName));
		}
		else {
			format = Rio.getParserFormatForMIMEType(contentType).orElseThrow(
					() -> new BadRequestException("Unknown Content-Type: " + contentType));
		}

		RepositoryConnection con = repository.getConnection();
		try {
			con.add(stream, baseURI, format, context);
		}
		catch (RDFParseException exc) {
			throw new BadRequestException(exc.getMessage(), exc);
		}
		catch (IllegalArgumentException exc) {
			throw new BadRequestException(exc.getMessage(), exc);
		}
		finally {
			con.close();
		}
	}

	private void add(URL url, String baseURI, String contentType, Resource... context)
		throws BadRequestException, RepositoryException, IOException
	{
		if (contentType == null) {
			throw new BadRequestException("No Content-Type provided");
		}

		RDFFormat format = null;
		if ("autodetect".equals(contentType)) {
			format = Rio.getParserFormatForFileName(url.getFile()).orElseThrow(
					() -> new BadRequestException("Could not automatically determine Content-Type for content: "
							+ url.getFile()));
		}
		else {
			format = Rio.getParserFormatForMIMEType(contentType).orElseThrow(
					() -> new BadRequestException("Unknown Content-Type: " + contentType));
		}

		try {
			RepositoryConnection con = repository.getConnection();
			try {
				con.add(url, baseURI, format, context);
			}
			finally {
				con.close();
			}
		}
		catch (RDFParseException exc) {
			throw new BadRequestException(exc.getMessage(), exc);
		}
		catch (MalformedURLException exc) {
			throw new BadRequestException(exc.getMessage(), exc);
		}
		catch (IllegalArgumentException exc) {
			throw new BadRequestException(exc.getMessage(), exc);
		}
	}

	@Override
	public void service(TupleResultBuilder builder, String xslPath)
		throws RepositoryException, QueryResultHandlerException
	{
		// TupleResultBuilder builder = getTupleResultBuilder(req, resp);
		builder.transform(xslPath, "add.xsl");
		builder.start();
		builder.link(Arrays.asList(INFO));
		builder.end();
	}

}