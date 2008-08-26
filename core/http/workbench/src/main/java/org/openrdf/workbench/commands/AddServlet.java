/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.exceptions.BadRequestException;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddServlet extends TransformationServlet {
	private Logger logger = LoggerFactory.getLogger(AddServlet.class);

	@Override
	public String[] getCookieNames() {
		return new String[] { "Content-Type" };
	}

	@Override
	protected void doPost(WorkbenchRequest req, HttpServletResponse resp,
			String xslPath) throws Exception, IOException {
		try {
			String baseURI = req.getParameter("baseURI");
			Resource context = req.getResource("context");
			String contentType = req.getParameter("Content-Type");
			if (req.isParameterPresent("context")) {
				if (req.isParameterPresent("url")) {
					add(req.getUrl("url"), baseURI, contentType, context);
				} else {
					add(req.getContentParameter(), baseURI, contentType,
							context);
				}
			} else {
				if (req.isParameterPresent("url")) {
					add(req.getUrl("url"), baseURI, contentType);
				} else {
					add(req.getContentParameter(), baseURI, contentType);
				}
			}
			resp.sendRedirect("summary");
		} catch (BadRequestException exc) {
			logger.warn(exc.toString(), exc);
			resp.setContentType("application/xml");
			PrintWriter out = resp.getWriter();
			TupleResultBuilder builder = new TupleResultBuilder(out);
			builder.transform(xslPath, "add.xsl");
			builder
					.start("error-message", "baseURI", "context",
							"Content-Type");
			builder.link("info");
			String baseURI = req.getParameter("baseURI");
			String context = req.getParameter("context");
			String contentType = req.getParameter("Content-Type");
			builder.result(exc.getMessage(), baseURI, context, contentType);
			builder.end();
		}
	}

	private void add(InputStream in, String baseURI, String contentType,
			Resource... context) throws Exception {
		if (contentType == null)
			throw new BadRequestException("No Content-Type provided");
		RDFFormat format = RDFFormat.forMIMEType(contentType);
		if (format == null)
			throw new BadRequestException("Unknown Content-Type: "
					+ contentType);
		RepositoryConnection con = repository.getConnection();
		try {
			con.add(in, baseURI, format, context);
		} catch (RDFParseException exc) {
			throw new BadRequestException(exc.getMessage());
		} catch (IllegalArgumentException exc) {
			throw new BadRequestException(exc.getMessage());
		} finally {
			con.close();
		}
	}

	private void add(URL url, String baseURI, String contentType,
			Resource... context) throws Exception {
		if (contentType == null)
			throw new BadRequestException("No Content-Type provided");
		RDFFormat format = RDFFormat.forMIMEType(contentType);
		if (format == null)
			throw new BadRequestException("Unknown Content-Type: "
					+ contentType);
		try {
			RepositoryConnection con = repository.getConnection();
			try {
				con.add(url, baseURI, format, context);
			} finally {
				con.close();
			}
		} catch (RDFParseException exc) {
			throw new BadRequestException(exc.getMessage());
		} catch (MalformedURLException exc) {
			throw new BadRequestException(exc.getMessage());
		} catch (IllegalArgumentException exc) {
			throw new BadRequestException(exc.getMessage());
		}
	}

	@Override
	public void service(PrintWriter out, String xslPath)
			throws RepositoryException {
		TupleResultBuilder builder = new TupleResultBuilder(out);
		builder.transform(xslPath, "add.xsl");
		builder.start();
		builder.link("info");
		builder.end();
	}

}